package de.rafael.bibliothek.controller.v1.group;

import de.rafael.bibliothek.classes.ApiController;
import de.rafael.bibliothek.configuration.AppConfiguration;
import de.rafael.bibliothek.database.model.Group;
import de.rafael.bibliothek.database.model.Project;
import de.rafael.bibliothek.database.model.Version;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.GroupRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Rafael K.
 * @since 17:41, 13.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupController extends ApiController {

    private static final CacheControl CACHE = defaultCache();

    @Autowired
    public GroupController(AppConfiguration configuration, ProjectRepository projects, VersionRepository versions, GroupRepository groups, BuildRepository builds) {
        super(configuration, projects, versions, groups, builds);
    }

    @GetMapping("/v1/projects/{project:" + Project.PATTERN + "}/group/{group:" + Group.PATTERN + "}")
    public ResponseEntity<?> group(@PathVariable("project") @Pattern(regexp = Project.PATTERN) String projectName, @PathVariable("group") @Pattern(regexp = Group.PATTERN) String groupId) {
        var project = super.findProject(projectName);
        var group = super.findGroup(project, groupId);
        var versions = super.versions.findAllByProjectAndGroup(project._id(), group._id()).stream().map(Version::name).toList();
        return ok(
                CACHE,
                new Response(
                        project.name(),
                        project.friendlyName(),
                        group.name(),
                        versions
                )
        );
    }

    private record Response(String project_id, String project_name, String group, List<String> versions) {}

}
