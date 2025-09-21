package app.api.dto;

import java.time.Instant;
import java.util.List;

public class EventCreateReq {
    public String title;
    public String description;
    public String place;
    public Instant startAt;
    public Instant endAt;
    public List<String> invites;
}

