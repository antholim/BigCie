package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.enums.BikeStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BikeRepository extends MongoRepository<Bike, UUID> {
    List<Bike> findByStatus(BikeStatus status);
    Bike findBikeById(UUID id);
}
