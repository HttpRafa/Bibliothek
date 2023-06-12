package de.rafael.bibliothek.controller.v1.build;

import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import de.rafael.bibliothek.throwables.BuildNotFound;
import de.rafael.bibliothek.throwables.ProjectNotFound;
import de.rafael.bibliothek.throwables.VersionNotFound;
import de.rafael.bibliothek.values.Patterns;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author Rafael K.
 * @since 22:08, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class BuildController {

    private static final CacheControl CACHE = CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));

    private final ProjectRepository projects;
    private final VersionRepository versions;
    private final BuildRepository builds;

    @Autowired
    public BuildController(ProjectRepository projects, VersionRepository versions, BuildRepository builds) {
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
    }

    @GetMapping("/v1/projects/{project:" + Patterns.PROJECT_NAME + "}/versions/{version:" + Patterns.VERSION_NAME + "}/builds/{build:" + Patterns.BUILD_NUMBER + "}")
    public ResponseEntity<?> build(@PathVariable("project") @Pattern(regexp = Patterns.PROJECT_NAME) String projectName, @PathVariable("version") @Pattern(regexp = Patterns.VERSION_NAME) String versionName, @PathVariable("build") @Pattern(regexp = Patterns.BUILD_NUMBER) int buildNumber) {
        var project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        var version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        var build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);
        return ResponseEntity.ok().cacheControl(CACHE).body(new Response(project.name(), project.friendlyName(), version.name(), build.number(), build.timestamp(), build.channel(), build.displayMode(), build.changes(), build.downloads()));
    }

    private record Response(String project_id, String project_name, String version, int build, Instant timestamp, Build.Channel channel, Build.DisplayMode displayMode, List<Build.Change> changes, Map<String, Build.Download> downloads) {}

}
