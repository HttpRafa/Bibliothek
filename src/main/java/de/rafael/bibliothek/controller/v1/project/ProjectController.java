package de.rafael.bibliothek.controller.v1.project;

import de.rafael.bibliothek.database.model.Group;
import de.rafael.bibliothek.database.model.Version;
import de.rafael.bibliothek.database.repository.GroupRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import de.rafael.bibliothek.throwables.ProjectNotFound;
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
 * @since 19:46, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    private static final CacheControl CACHE = CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));

    private final ProjectRepository projects;
    private final GroupRepository groups;
    private final VersionRepository versions;

    @Autowired
    public ProjectController(ProjectRepository projects, GroupRepository groups, VersionRepository versions) {
        this.projects = projects;
        this.groups = groups;
        this.versions = versions;
    }

    @GetMapping("/v1/projects/{project:" + Patterns.PROJECT_NAME + "}")
    public ResponseEntity<?> project(@PathVariable("project") @Pattern(regexp = Patterns.PROJECT_NAME) String projectName) {
        var project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
        var groups = this.groups.findAllByProject(project._id()).stream().sorted(Group.COMPARATOR).map(Group::name).toList();
        var versions = this.versions.findAllByProject(project._id()).stream().sorted(Version.COMPARATOR).map(Version::name).toList();
        return ResponseEntity.ok().cacheControl(CACHE).body(new Response(project.name(), project.friendlyName(), groups, versions));
    }

    private record Response(String project_id, String project_name, List<String> groups, List<String> versions) {}

}
