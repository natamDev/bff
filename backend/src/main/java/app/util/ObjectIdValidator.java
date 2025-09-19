package app.util;

import org.bson.types.ObjectId;

public class ObjectIdValidator {
    public static void validate(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new IllegalArgumentException("Invalid ObjectId: " + (id == null ? "null" : id));
        }
    }
}