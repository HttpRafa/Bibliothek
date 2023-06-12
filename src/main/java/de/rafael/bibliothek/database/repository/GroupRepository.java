package de.rafael.bibliothek.database.repository;

import de.rafael.bibliothek.database.model.Group;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Rafael K.
 * @since 19:35, 12.06.23
 */

@Repository
public interface GroupRepository extends MongoRepository<Group, ObjectId> {

    List<Group> findAllByProject(final ObjectId project);

    Optional<Group> findByProjectAndName(final ObjectId project, final String name);

}
