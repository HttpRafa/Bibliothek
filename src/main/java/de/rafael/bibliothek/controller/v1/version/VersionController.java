package de.rafael.bibliothek.controller.v1.version;

import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
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
import java.util.List;

/**
 * @author Rafael K.
 * @since 21:47, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class VersionController {

    private static final CacheControl CACHE = CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));

    private final ProjectRepository projects;
    private final VersionRepository versions;
    private final BuildRepository builds;

    @Autowired
    public VersionController(ProjectRepository projects, VersionRepository versions, BuildRepository builds) {
        this.projects = projects;
        this.versions = versions;
        this.builds = builds;
    }

    @GetMapping("/v1/projects/{project:" + Patterns.PROJECT_NAME + "}/versions/{version:" + Patterns.VERSION_NAME + "}")
    public ResponseEntity<?> version(@PathVariable("project") @Pattern(regexp = Patterns.PROJECT_NAME) String projectName, @PathVariable("version") @Pattern(regexp = Patterns.VERSION_NAME) String versionName) {
        var project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        var version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
        var builds = this.builds.findAllByProjectAndVersion(project._id(), version._id());
        return ResponseEntity.ok().cacheControl(CACHE).body(new Response(project.name(), project.friendlyName(), version.name(), builds.stream().map(Build::number).toList()));
    }

    private record Response(String project_id, String project_name, String version, List<Integer> builds) {}

}
