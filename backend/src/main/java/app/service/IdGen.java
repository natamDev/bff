package app.service;

import java.security.SecureRandom;
import java.util.Base64;

public class IdGen {
  private static final SecureRandom RND = new SecureRandom();
  public static String randomId(int bytes){
    byte[] b = new byte[bytes];
    RND.nextBytes(b);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }
  public static String shortId(){ return randomId(9); }   // ~12 chars
  public static String secret(){ return randomId(18); }   // ~24 chars
}
