package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends MongoRepository<Trip, UUID> {
    List<Trip> findByUserId(UUID userId);
}
