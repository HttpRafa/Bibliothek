/*
 * MIT License
 *
 * Copyright (c) 2023 Rafael
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.rafael.bibliothek.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.intellij.lang.annotations.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Rafael K.
 * @since 19:13, 12.06.23
 */

@CompoundIndex(def = "{'project': 1, 'version': 1}")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'number': 1}")
@Document(collection = "builds")
public record Build(@Id ObjectId _id, ObjectId project, ObjectId version, int number, Instant timestamp, List<Change> changes, Map<String, Download> downloads, @JsonProperty Channel channel, @JsonProperty DisplayMode displayMode) {

    public static final String PATTERN = "\\d+";

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
