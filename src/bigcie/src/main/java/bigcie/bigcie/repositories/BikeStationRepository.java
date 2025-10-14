package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BikeStationRepository extends MongoRepository<BikeStation, UUID> {
    List<BikeStation> findByStatus(BikeStationStatus status);
}
