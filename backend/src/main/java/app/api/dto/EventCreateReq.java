package app.api.dto;

import java.util.List;

public class EventCreateReq {
    public String title;
    public String description;
    public String place;
    public String startAt;
    public String endAt;
    public List<String> invites;
}

