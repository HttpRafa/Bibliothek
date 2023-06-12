package de.rafael.bibliothek.database.repository;

import de.rafael.bibliothek.database.model.Build;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Rafael K.
 * @since 19:29, 12.06.23
 */

@Repository
public interface BuildRepository extends MongoRepository<Build, ObjectId>  {

    List<Build> findAllByProjectAndVersion(final ObjectId project, final ObjectId version);

    List<Build> findAllByProjectAndVersionIn(final ObjectId project, final Collection<ObjectId> version);

    Optional<Build> findByProjectAndVersionAndNumber(final ObjectId project, final ObjectId version, final int number);

}
