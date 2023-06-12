package de.rafael.bibliothek.controller.v1.project;

import de.rafael.bibliothek.database.model.Project;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * @author Rafael K.
 * @since 21:40, 12.06.23
 */

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectsController {

    private static final CacheControl CACHE = CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));

    private final ProjectRepository projects;

    @Autowired
    public ProjectsController(ProjectRepository projects) {
        this.projects = projects;
    }

    @GetMapping("/v1/projects")
    public ResponseEntity<?> projects() {
        return ResponseEntity.ok().cacheControl(CACHE).body(new Response(this.projects.findAll().stream().map(Project::name).toList()));
    }

    private record Response(List<String> projects) {}

}
