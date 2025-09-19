package app.service;

import app.domain.Event;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ConfigMapping(prefix = "app")
interface AppConfig {
  @WithName("link.secret") String linkSecret();
  @WithName("base.url") String baseUrl();
}

@ApplicationScoped
public class LinkFactory {
  @Inject AppConfig cfg;
  private String hmac(String payload){
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(cfg.linkSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e){ throw new RuntimeException(e); }
  }
  public String hostLink(Event e){
    String sig = hmac(e.id.toHexString() + "|" + e.hostSecret);
    return cfg.baseUrl()+"/#/host/" + e.id.toHexString() + "?sig=" + sig;
  }
  public String inviteLink(Event e, String inviteId){
    String sig = hmac(e.id.toHexString() + "|" + inviteId);
    return cfg.baseUrl()+"/#/invite/" + e.id.toHexString() + "/" + inviteId + "?sig=" + sig;
  }
  public boolean verifyHost(String eventId, String sig, String hostSecret){ return hmac(eventId + "|" + hostSecret).equals(sig); }
  public boolean verifyInvite(String eventId, String sig, String inviteId){ return hmac(eventId + "|" + inviteId).equals(sig); }
}
