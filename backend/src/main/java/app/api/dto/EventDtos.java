package app.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class EventCreateReq {
  public String title;
  public String description;
  public String place;
  public OffsetDateTime startAt;
  public OffsetDateTime endAt;
  public List<String> invites;
}
public class EventCreatedRes { public String eventId; public String hostLink; public Map<String,String> inviteLinks; }
public class BetCreateReq { public String text; }
public class BetUpdateReq { public String status; }
public class BetPredictionReq { public String choice; }
