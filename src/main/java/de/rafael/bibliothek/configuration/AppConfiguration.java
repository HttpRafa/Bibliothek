package de.rafael.bibliothek.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.nio.file.Path;

/**
 * @author Rafael K.
 * @since 18:57, 12.06.23
 */

@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
public class AppConfiguration {

    private URL baseUrl;
    private Path storagePath;

}
