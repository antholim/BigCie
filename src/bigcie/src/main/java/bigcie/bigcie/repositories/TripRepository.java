package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends MongoRepository<Trip, UUID> {
    @Query("{ '_class': 'trip', 'userId': ?0 }")
    List<Trip> findByUserId(UUID userId);

    @Query("{ '_class': 'trip', 'userId': ?0 }")
    void deleteByUserId(UUID userId);
}
