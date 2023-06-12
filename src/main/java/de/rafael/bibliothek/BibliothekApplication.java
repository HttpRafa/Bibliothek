package de.rafael.bibliothek;

import de.rafael.bibliothek.configuration.AppConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Rafael K.
 * @since 18:57, 12.06.23
 */

@EnableConfigurationProperties(AppConfiguration.class)
@SpringBootApplication
public class BibliothekApplication {

	public static void main(String[] args) {
		SpringApplication.run(BibliothekApplication.class, args);
	}

}
