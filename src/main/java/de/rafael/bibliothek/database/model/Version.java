package de.rafael.bibliothek.database.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Comparator;

/**
 * @author Rafael K.
 * @since 19:13, 12.06.23
 */

@CompoundIndex(def = "{'project': 1, 'group': 1}")
@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "versions")
public record Version(@Id ObjectId _id, ObjectId project, ObjectId group, String name, Instant timestamp) {

    public static final Comparator<Version> COMPARATOR = Comparator.comparing(Version::timestamp);

}
