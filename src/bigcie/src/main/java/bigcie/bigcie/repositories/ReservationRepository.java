package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, UUID> {
    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findByBikeStationId(UUID bikeStationId);
}
