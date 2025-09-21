package app.api;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/") // racine
public class CorsPreflightResource {
  // match /events, /events/..., etc.
  @OPTIONS
  @Path("{any: .*}")
  public Response preflight() {
    // Quarkus ajoutera les bons headers CORS d'après ta conf
    return Response.noContent().build(); // 204
  }
}
