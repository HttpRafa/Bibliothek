package de.rafael.bibliothek.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.intellij.lang.annotations.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author Rafael K.
 * @since 19:13, 12.06.23
 */

@CompoundIndex(def = "{'project': 1, 'version': 1}")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'number': 1}")
@Document(collection = "builds")
public record Build(@Id ObjectId _id, ObjectId project, ObjectId version, int number, Instant timestamp, List<Change> changes, Map<String, Download> downloads, @JsonProperty Channel channel, @JsonProperty DisplayMode displayMode) {

    public enum Channel {

        @JsonProperty("default")
        DEFAULT,
        @JsonProperty("experimental")
        EXPERIMENTAL

    }

    public enum DisplayMode  {

        @JsonProperty("hide")
        HIDE,
        @JsonProperty("promote")
        PROMOTE

    }

    public record Change(String commit, String summary, String message) {}

    public record Download(String name, String sha256) {

        @Language("RegExp")
        public static final String PATTERN = "[a-zA-Z0-9._-]+";

    }

}
