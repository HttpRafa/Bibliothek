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
package de.rafael.bibliothek.classes;

import de.rafael.bibliothek.configuration.AppConfiguration;
import de.rafael.bibliothek.database.model.Build;
import de.rafael.bibliothek.database.model.Group;
import de.rafael.bibliothek.database.model.Project;
import de.rafael.bibliothek.database.model.Version;
import de.rafael.bibliothek.database.repository.BuildRepository;
import de.rafael.bibliothek.database.repository.GroupRepository;
import de.rafael.bibliothek.database.repository.ProjectRepository;
import de.rafael.bibliothek.database.repository.VersionRepository;
import de.rafael.bibliothek.throwables.BuildNotFound;
import de.rafael.bibliothek.throwables.ProjectNotFound;
import de.rafael.bibliothek.throwables.VersionNotFound;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class ApiController {

    public static final MediaType JAVA_ARCHIVE = new MediaType("application", "java-archive");

    protected final AppConfiguration configuration;
    protected final ProjectRepository projects;
    protected final VersionRepository versions;
    protected final GroupRepository groups;
    protected final BuildRepository builds;

    public ApiController(AppConfiguration configuration, ProjectRepository projects, VersionRepository versions, GroupRepository groups, BuildRepository builds) {
        this.configuration = configuration;
        this.projects = projects;
        this.versions = versions;
        this.groups = groups;
        this.builds = builds;
    }

    protected <T> ResponseEntity<T> ok(CacheControl cache, T response) {
        return ResponseEntity.ok().cacheControl(cache).body(response);
    }

    protected Project findProject(@NotNull String id) {
        return this.projects.findById(id).orElseThrow(ProjectNotFound::new);
    }

    protected Version findVersion(@NotNull Project project, @NotNull String id) {
        return this.versions.findByProjectAndId(project._id(), id).orElseThrow(VersionNotFound::new);
    }

    protected Build findBuild(@NotNull Project project, @NotNull Version version, int number) {
        return this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), number).orElseThrow(BuildNotFound::new);
    }

    protected Stream<Group> findGroupsAsStream(@NotNull Project project) {
        return this.groups.findAllByProject(project._id()).stream().sorted(Group.COMPARATOR);
    }

    protected Stream<Version> findVersionsAsStream(@NotNull Project project) {
        return this.versions.findAllByProject(project._id()).stream().sorted(Version.COMPARATOR);
    }

    protected List<Project> findProjects() {
        return this.projects.findAll();
    }

    public static @NotNull CacheControl defaultCache() {
        return CacheControl.empty().cachePublic().sMaxAge(Duration.ofMinutes(30));
    }

}
