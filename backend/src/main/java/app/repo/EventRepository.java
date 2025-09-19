package app.repo;

import app.domain.Event;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

@ApplicationScoped
public class EventRepository implements PanacheMongoRepository<Event> {
  public Event byId(ObjectId id){ return findById(id); }
}
