package de.rafael.bibliothek.database.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Comparator;

/**
 * @author Rafael K.
 * @since 19:27, 12.06.23
 */

@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "groups")
public record Group(@Id ObjectId _id, ObjectId project, String name, Instant timestamp) {

    public static final Comparator<Group> COMPARATOR = Comparator.comparing(Group::timestamp);

}
