package app.domain;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@MongoEntity(collection = "events")
public class Event {
    public ObjectId id;
    public String title;
    public String description;
    public String place;
    @BsonProperty("startAt")
    public OffsetDateTime startAt;
    @BsonProperty("endAt")
    public OffsetDateTime endAt;
    public String hostSecret;
    public boolean closed = false;

    public List<Invite> invites = new ArrayList<>();
    public List<Bet> bets = new ArrayList<>();

    @BsonProperty("startAt")
    public String getStartAt() {
        return startAt != null ? startAt.toString() : null;
    }

    @BsonProperty("startAt")
    public void setStartAt(String startAt) {
        this.startAt = startAt != null ? OffsetDateTime.parse(startAt) : null;
    }

    @BsonProperty("endAt")
    public String getEndAt() {
        return endAt != null ? endAt.toString() : null;
    }

    @BsonProperty("endAt")
    public void setEndAt(String endAt) {
        this.endAt = endAt != null ? OffsetDateTime.parse(endAt) : null;
    }

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
        public OffsetDateTime at;

        public enum Choice {YES, NO}
    }
}
