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

@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PingResource {
  @GET public String ping() { return "pong"; }
}