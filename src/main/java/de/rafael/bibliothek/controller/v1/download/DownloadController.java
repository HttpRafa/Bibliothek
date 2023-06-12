package de.rafael.bibliothek.controller.v1.download;

import de.rafael.bibliothek.configuration.AppConfiguration;
import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import de.rafael.bibliothek.throwables.*;
import de.rafael.bibliothek.values.Patterns;
import jakarta.validation.constraints.Pattern;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * @author Rafael K.
 * @since 22:28, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DownloadController {

    private static final CacheControl CACHE = CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));

    private final AppConfiguration configuration;
    private final ProjectRepository projects;
    private final VersionRepository versions;
    private final BuildRepository builds;

    public DownloadController(AppConfiguration configuration, ProjectRepository projects, VersionRepository versions, BuildRepository builds) {
        this.configuration = configuration;
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
    }

    @GetMapping(
            value = "/v1/projects/{project:" + Patterns.PROJECT_NAME + "}/versions/{version:" + Patterns.VERSION_NAME + "}/builds/{build:" + Patterns.BUILD_NUMBER + "}/downloads/{download:" + Patterns.DOWNLOAD_NAME + "}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    "application/java-archive"
            }
    )
    public ResponseEntity<?> download(@PathVariable("project") @Pattern(regexp = Patterns.PROJECT_NAME) String projectName, @PathVariable("version") @Pattern(regexp = Patterns.VERSION_NAME) String versionName, @PathVariable("build") @Pattern(regexp = Patterns.BUILD_NUMBER) int buildNumber, @PathVariable("download") @Pattern(regexp = Patterns.DOWNLOAD_NAME) String downloadName) {
        try {
            var project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
            var version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
            var build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);
            var filteredDownloads = build.downloads().entrySet().stream().filter(entry -> entry.getValue().name().equals(downloadName)).toList();
            for (Map.Entry<String, Build.Download> entry : filteredDownloads) {
                return new JavaArchive(this.configuration.getStoragePath().resolve(project.name()).resolve(version.name()).resolve(String.valueOf(build.number())).resolve(entry.getValue().name()), CACHE);
            }
            throw new DownloadNotFound();
        } catch (Throwable throwable) {
            throw new DownloadFailed(throwable);
        }
    }

    private static class JavaArchive extends ResponseEntity<FileSystemResource> {

        public JavaArchive(Path path, CacheControl cache) throws IOException {
            super(new FileSystemResource(path), headersFor(path, cache), HttpStatus.OK);
        }

        private static @NotNull HttpHeaders headersFor(final @NotNull Path path, final CacheControl cache) throws IOException {
            final HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(cache);
            headers.setContentDisposition(ContentDisposition.attachment().filename(path.getFileName().toString(), StandardCharsets.UTF_8).build());
            headers.setContentType(new MediaType("application", "java-archive"));
            headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
            return headers;
        }

    }

}
