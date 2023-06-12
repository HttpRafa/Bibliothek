package de.rafael.bibliothek.database.repository;

import de.rafael.bibliothek.database.model.Version;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Rafael K.
 * @since 19:33, 12.06.23
 */

@Repository
public interface VersionRepository extends MongoRepository<Version, ObjectId> {

    List<Version> findAllByProject(final ObjectId project);

    List<Version> findAllByProjectAndGroup(final ObjectId project, final ObjectId group);

    Optional<Version> findByProjectAndName(final ObjectId project, final String name);

}
