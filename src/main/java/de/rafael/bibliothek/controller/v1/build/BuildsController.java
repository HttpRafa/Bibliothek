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
package de.rafael.bibliothek.controller.v1.build;

import de.rafael.bibliothek.classes.ApiController;
import de.rafael.bibliothek.configuration.AppConfiguration;
import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.model.Project;
import de.rafael.bibliothek.database.model.Version;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.GroupRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rafael K.
 * @since 22:08, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class BuildsController extends ApiController {

    private static final CacheControl CACHE = defaultCache();

    @Autowired
    public BuildsController(AppConfiguration configuration, ProjectRepository projects, VersionRepository versions, GroupRepository groups, BuildRepository builds) {
        super(configuration, projects, versions, groups, builds);
    }

    @GetMapping("/v1/projects/{project:" + Project.PATTERN + "}/versions/{version:" + Version.PATTERN + "}/builds")
    public ResponseEntity<?> build(@PathVariable("project") @Pattern(regexp = Project.PATTERN) String projectId, @PathVariable("version") @Pattern(regexp = Version.PATTERN) String versionId) {
        var project = super.findProject(projectId);
        var version = super.findVersion(project, versionId);
        var builds = this.builds.findAllByProjectAndVersion(project._id(), version._id()).stream().map(build ->
                new ResponseBuild(
                        build.number(),
                        build.timestamp(),
                        build.channel(),
                        build.displayMode(),
                        build.changes(),
                        build.downloads()
                )).toList();
        return ok(
                CACHE,
                new Response(
                    project.id(),
                    project.name(),
                    version.id(),
                    builds
                )
        );
    }

    private record Response(String project_id, String project_name, String version, List<ResponseBuild> builds) {}

    private record ResponseBuild(int build, Instant timestamp, Build.Channel channel, Build.DisplayMode displayMode, List<Build.Change> changes, Map<String, Build.Download> downloads) {}

}
