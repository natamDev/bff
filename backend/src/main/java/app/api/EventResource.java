package app.api;

import app.api.dto.BetCreateReq;
import app.api.dto.BetPredictionReq;
import app.api.dto.BetUpdateReq;
import app.api.dto.EventCreateReq;
import app.api.dto.EventCreatedRes;
import app.domain.Event;
import app.repo.EventRepository;
import app.service.IdGen;
import app.service.LinkFactory;
import app.util.ObjectIdValidator;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.ws.rs.OPTIONS;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Event", description = "Manage events and bets")
public class EventResource {

    @Inject
    EventRepository repo;
    @Inject
    LinkFactory links;

    @POST
    @Transactional
    @Operation(summary = "Create a new event")
    public Response create(EventCreateReq req) {
        Log.info("Create event");
        if (req.title == null || req.title.isBlank()) throw new BadRequestException("title required");
        if (req.startAt == null || req.endAt == null)
            throw new BadRequestException("startAt and endAt must be provided");

        Log.infof("startAt: %s, endAt: %s", req.startAt, req.endAt);

        Event e = new Event();
        e.id = new ObjectId();
        e.title = req.title;
        e.description = req.description;
        e.place = req.place;
        e.startAt = req.startAt;
        e.endAt = req.endAt;
        e.hostSecret = IdGen.secret();
        if (req.invites != null) {
            for (String name : req.invites) {
                Event.Invite inv = new Event.Invite();
                inv.id = IdGen.shortId();
                inv.name = name;
                inv.revoked = false;
                e.invites.add(inv);
            }
        }
        repo.persist(e);
        Map<String, String> inviteLinks = e.invites.stream().collect(Collectors.toMap(i -> i.name, i -> links.inviteLink(e, i.id)));
        EventCreatedRes res = new EventCreatedRes();
        res.eventId = e.id.toHexString();
        res.hostLink = links.hostLink(e);
        res.inviteLinks = inviteLinks;
        return Response.ok(res).build();
    }

    @GET
    @Path("/{eventId}")
    public EventView get(@PathParam("eventId") String eventId) {
        ObjectIdValidator.validate(eventId); // Valide l'identifiant
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        return EventView.of(e);
    }

    @GET
    @Path("/{eventId}/as-invite")
    public InviteEventView getAsInvite(@PathParam("eventId") String eventId,
                                       @QueryParam("inviteId") String inviteId,
                                       @QueryParam("sig") String sig) {
        ObjectIdValidator.validate(eventId); // Valide l'identifiant
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyInvite(eventId, sig, inviteId)) throw new ForbiddenException();
        return InviteEventView.of(e, inviteId);
    }

    @POST
    @Path("/{eventId}/bets")
    @Transactional
    public EventView addBet(@PathParam("eventId") String eventId, @QueryParam("sig") String sig, BetCreateReq req) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();
        if (req.text == null || req.text.isBlank()) throw new BadRequestException("text required");
        Event.Bet b = new Event.Bet();
        b.id = IdGen.shortId();
        b.text = req.text;
        e.bets.add(b);
        repo.update(e);
        return EventView.of(e);
    }

    @PATCH
    @Path("/{eventId}/bets/{betId}")
    @Transactional
    public EventView setBetStatus(@PathParam("eventId") String eventId, @PathParam("betId") String betId, @QueryParam("sig") String sig, BetUpdateReq req) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();
        var bet = e.bets.stream().filter(b -> b.id.equals(betId)).findFirst().orElseThrow(() -> new NotFoundException("bet not found"));
        switch (req.status) {
            case "open" -> bet.status = Event.Bet.Status.open;
            case "true" -> bet.status = Event.Bet.Status._true;
            case "false" -> bet.status = Event.Bet.Status._false;
            default -> throw new BadRequestException("status invalid");
        }
        repo.update(e);
        return EventView.of(e);
    }

    @DELETE
    @Path("/{eventId}/bets/{betId}")
    @Transactional
    public EventView deleteBet(@PathParam("eventId") String eventId,
                            @PathParam("betId") String betId,
                            @QueryParam("sig") String sig) {
        // Vérifier que l'event existe
        ObjectId oid = new ObjectId(eventId);
        Event e = repo.byId(oid);
        if (e == null) throw new NotFoundException("event not found");

        // Vérifier que c'est bien l'hôte
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();

        // Supprimer le bet (et donc ses predictions)
        boolean removed = e.bets.removeIf(b -> b.id.equals(betId));
        if (!removed) throw new NotFoundException("bet not found");

        // Sauvegarde de l'event modifié
        repo.update(e);

        // Retourne l'event mis à jour
        return EventView.of(e);
    }


    @POST
    @Path("/{eventId}/bets/{betId}/predict")
    @Transactional
    public EventView predict(@PathParam("eventId") String eventId, @PathParam("betId") String betId,
                             @QueryParam("inviteId") String inviteId, @QueryParam("sig") String sig, BetPredictionReq req) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyInvite(eventId, sig, inviteId)) throw new ForbiddenException();
        var bet = e.bets.stream().filter(b -> b.id.equals(betId)).findFirst().orElseThrow(() -> new NotFoundException("bet not found"));
        if (bet.status != Event.Bet.Status.open) throw new BadRequestException("bet closed");
        var choice = switch (req.choice) {
            case "Oui" -> Event.Prediction.Choice.YES;
            case "Non" -> Event.Prediction.Choice.NO;
            default -> throw new BadRequestException("choice must be YES or NO");
        };
        var existing = bet.predictions.stream().filter(p -> p.inviteId.equals(inviteId)).findFirst();
        if (existing.isPresent()) {
            existing.get().choice = choice;
            existing.get().at = Instant.now().toString();
        } else {
            Event.Prediction p = new Event.Prediction();
            p.inviteId = inviteId;
            p.choice = choice;
            p.at = Instant.now().toString();
            bet.predictions.add(p);
        }
        repo.update(e);
        return EventView.of(e);
    }

    @GET
    @Path("/{eventId}/invites/links")
    public List<InviteLinkView> listInviteLinks(@PathParam("eventId") String eventId,
                                                @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();

        return e.invites.stream()
                .map(i -> {
                    InviteLinkView v = new InviteLinkView();
                    v.id = i.id;
                    v.name = i.name;
                    v.revoked = i.revoked;
                    v.url = links.inviteLink(e, i.id); // lien seulement si pas révoqué
                    return v;
                })
                .toList();
    }

    public static class InviteLinkView {
        public String id;
        public String name;
        public boolean revoked;
        public String url;
    }

    @POST
    @Path("/{eventId}/invites")
    @Transactional
    public EventView addInvite(@PathParam("eventId") String eventId,
                            @QueryParam("sig") String sig,
                            Map<String, String> body) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();

        String name = body.get("name");
        if (name == null || name.isBlank()) throw new BadRequestException("name required");

        Event.Invite inv = new Event.Invite();
        inv.id = IdGen.shortId();
        inv.name = name;
        inv.revoked = false;
        e.invites.add(inv);

        repo.update(e);
        return EventView.of(e);
    }

    @PATCH
    @Path("/{eventId}/invites/{inviteId}/revoke")
    @Transactional
    public EventView revokeInvite(@PathParam("eventId") String eventId,
                                @PathParam("inviteId") String inviteId,
                                @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();

        Event.Invite inv = e.invites.stream()
            .filter(i -> i.id.equals(inviteId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("invite not found"));

        inv.revoked = true;

        repo.update(e);
        return EventView.of(e);
    }

    @PATCH
    @Path("/{eventId}/invites/{inviteId}/approve")
    @Transactional
    public EventView approveInvite(@PathParam("eventId") String eventId,
                                @PathParam("inviteId") String inviteId,
                                @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();

        Event.Invite inv = e.invites.stream()
            .filter(i -> i.id.equals(inviteId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("invite not found"));

        inv.revoked = false;

        repo.update(e);
        return EventView.of(e);
    }

    @GET
    @Path("/{eventId}/predictions/matrix")
    public List<MatrixRow> matrix(@PathParam("eventId") String eventId, @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();
        Map<String, String> names = e.invites.stream().collect(Collectors.toMap(i -> i.id, i -> i.name));
        List<MatrixRow> rows = new ArrayList<>();
        for (Event.Bet b : e.bets) {
            MatrixRow r = new MatrixRow();
            r.betId = b.id;
            r.text = b.text;
            r.status = switch (b.status) {
                case open -> "open";
                case _true -> "true";
                case _false -> "false";
            };
            r.answers = new LinkedHashMap<>();
            for (Event.Prediction p : b.predictions) {
                String name = names.getOrDefault(p.inviteId, p.inviteId);
                r.answers.put(name, p.choice.name());
            }
            rows.add(r);
        }
        return rows;
    }

    @POST
    @Path("/{eventId}/close")
    @Transactional
    public EventView close(@PathParam("eventId") String eventId, @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();
        e.closed = true;
        repo.update(e);
        return EventView.of(e);
    }

    @POST
    @Path("/{eventId}/open")
    @Transactional
    public EventView open(@PathParam("eventId") String eventId, @QueryParam("sig") String sig) {
        Event e = repo.byId(new ObjectId(eventId));
        if (e == null) throw new NotFoundException();
        if (!links.verifyHost(eventId, sig, e.hostSecret)) throw new ForbiddenException();
        e.closed = false;
        repo.update(e);
        return EventView.of(e);
    }

    // Views
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventView {
        public String id, title, description, place, startAt, endAt;
        public boolean closed;
        public List<BetView> bets;

        static EventView of(Event e) {
            EventView v = new EventView();
            v.id = e.id.toHexString();
            v.title = e.title;
            v.description = e.description;
            v.place = e.place;
            v.startAt = e.startAt != null ? e.startAt.toString() : null;
            v.endAt = e.endAt != null ? e.endAt.toString() : null;
            v.closed = e.closed;
            v.bets = e.bets.stream().map(BetView::of).toList();
            return v;
        }
    }

    public static class BetView {
        public String id, text, status;

        static BetView of(Event.Bet b) {
            BetView v = new BetView();
            v.id = b.id;
            v.text = b.text;
            v.status = switch (b.status) {
                case open -> "open";
                case _true -> "true";
                case _false -> "false";
            };
            return v;
        }
    }

    public static class InviteEventView {
        public String id, title, description, place, startAt, endAt;
        public boolean closed;
        public boolean revoked;
        public List<InviteBetRow> bets;

        static InviteEventView of(Event e, String inviteId) {
            InviteEventView v = new InviteEventView();
            v.id = e.id.toHexString();
            v.title = e.title;
            v.description = e.description;
            v.place = e.place;
            v.startAt = e.startAt != null ? e.startAt.toString() : null;
            v.endAt = e.endAt != null ? e.endAt.toString() : null;
            v.closed = e.closed;
            var invite = e.invites.stream()
            .filter(i -> i.id.equals(inviteId))
            .findFirst()
            .orElse(null);
            v.revoked = (invite != null && invite.revoked);
            v.bets = e.bets.stream().map(b -> {
                InviteBetRow r = new InviteBetRow();
                r.id = b.id;
                r.text = b.text;
                r.status = switch (b.status) {
                    case open -> "open";
                    case _true -> "true";
                    case _false -> "false";
                };
                r.myChoice = b.predictions.stream().filter(p -> p.inviteId.equals(inviteId)).map(p -> p.choice.name()).findFirst().orElse(null);
                return r;
            }).toList();
            return v;
        }
    }

    public static class InviteBetRow {
        public String id, text, status, myChoice;
    }

    public static class MatrixRow {
        public String betId, text, status;
        public Map<String, String> answers;
    }
}
