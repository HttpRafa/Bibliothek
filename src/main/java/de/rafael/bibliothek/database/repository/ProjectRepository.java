package de.rafael.bibliothek.database.repository;

import de.rafael.bibliothek.database.model.Project;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Rafael K.
 * @since 19:32, 12.06.23
 */

@Repository
public interface ProjectRepository extends MongoRepository<Project, ObjectId> {

    Optional<Project> findByName(final String name);

}
