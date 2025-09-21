package app.domain;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "events")
public class Event {
    public ObjectId id;
    public String title;
    public String description;
    public String place;
    public Instant startAt;
    public Instant endAt;
    public String hostSecret;
    public boolean closed = false;

    public List<Invite> invites = new ArrayList<>();
    public List<Bet> bets = new ArrayList<>();


    public static class Invite {
        public String id;
        public String name;
        public boolean revoked;
    }

    public static class Bet {
        public String id;
        public String text;
        public Status status = Status.open;
        public List<Prediction> predictions = new ArrayList<>();

        public enum Status {open, _true, _false}
    }

    public static class Prediction {
        public String inviteId;
        public Choice choice;
        public Instant at; // juste le champ public, Panache gère ça

        public enum Choice {YES, NO}
    }
}
