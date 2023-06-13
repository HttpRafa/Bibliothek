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
package de.rafael.bibliothek.controller.v1.download;

import de.rafael.bibliothek.classes.ApiController;
import de.rafael.bibliothek.configuration.AppConfiguration;
import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.model.Project;
import de.rafael.bibliothek.database.model.Version;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.GroupRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import de.rafael.bibliothek.throwables.*;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rafael K.
 * @since 22:28, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DownloadController extends ApiController {

    private static final CacheControl CACHE = defaultCache();

    @Autowired
    public DownloadController(AppConfiguration configuration, ProjectRepository projects, VersionRepository versions, GroupRepository groups, BuildRepository builds) {
        super(configuration, projects, versions, groups, builds);
    }

    @GetMapping(
            value = "/v1/projects/{project:" + Project.PATTERN + "}/versions/{version:" + Version.PATTERN + "}/builds/{build:" + Build.PATTERN + "}/downloads/{download:" + Build.Download.PATTERN + "}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    "application/java-archive"
            }
    )
    public ResponseEntity<?> download(@PathVariable("project") @Pattern(regexp = Project.PATTERN) String projectName, @PathVariable("version") @Pattern(regexp = Version.PATTERN) String versionName, @PathVariable("build") @Pattern(regexp = Build.PATTERN) int buildNumber, @PathVariable("download") @Pattern(regexp = Build.Download.PATTERN) String downloadName) {
        try {
            var project = super.findProject(projectName);
            var version = super.findVersion(project, versionName);
            var build = super.findBuild(project, version, buildNumber);
            var filteredDownloads = build.downloads().entrySet().stream()
                    .filter(entry -> entry.getValue().name().equals(downloadName))
                    .toList();
            for (Map.Entry<String, Build.Download> entry : filteredDownloads) {
                return new JavaArchive(
                        this.configuration.getStoragePath()
                                .resolve(project.name())
                                .resolve(version.name())
                                .resolve(String.valueOf(build.number()))
                                .resolve(entry.getValue().name()),
                        CACHE
                );
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
            headers.setContentType(JAVA_ARCHIVE);
            headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
            return headers;
        }

    }

}
