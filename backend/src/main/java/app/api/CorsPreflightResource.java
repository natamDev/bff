package app.api;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Response;

@Path("/")
public class CorsPreflightResource {

  private static final String ALLOWED_ORIGIN = "https://storied-youtiao-971729.netlify.app";

  // Attrape /events, /events/..., etc.
  @OPTIONS
  @Path("{any: .*}")
  public Response preflight(@HeaderParam("Origin") String origin,
                            @HeaderParam("Access-Control-Request-Method") String reqMethod,
                            @HeaderParam("Access-Control-Request-Headers") String reqHeaders) {

    // Optionnel: sécurise l'origine
    if (!ALLOWED_ORIGIN.equals(origin)) {
      return Response.status(403).build();
    }

    return Response.noContent()
        .header("Access-Control-Allow-Origin", ALLOWED_ORIGIN)
        .header("Vary", "Origin")
        .header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
        .header("Access-Control-Allow-Headers",
            (reqHeaders != null && !reqHeaders.isBlank())
                ? reqHeaders
                : "Content-Type,Authorization,Origin,Accept,X-Requested-With")
        .header("Access-Control-Allow-Credentials", "true")
        .header("Access-Control-Max-Age", "86400")
        .build();
  }
}
