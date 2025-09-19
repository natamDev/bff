package app.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class EventCreateReq {
  public String title;
  public String description;
  public String place;
  public OffsetDateTime startAt;
  public OffsetDateTime endAt;
  public List<String> invites;
}

