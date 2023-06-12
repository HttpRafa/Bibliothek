package de.rafael.bibliothek.database.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Rafael K.
 * @since 19:13, 12.06.23
 */

@Document(collection = "projects")
public record Project(@Id ObjectId _id, @Indexed String name, String friendlyName) {}
