package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, UUID> {
    List<Reservation> findByUserId(UUID userId);

    Reservation findByBikeId(UUID bikeId);

    /**
     * Query MongoDB for a reservation by bike ID
     * 
     * @param bikeId The ID of the bike
     * @return List of reservations for the specified bike
     */
    List<Reservation> findAllByBikeId(UUID bikeId);

    /**
     * Query MongoDB for expired reservations based on expiry time
     * 
     * @param expiryTime The current time to compare against
     * @return List of expired reservations
     */
    @Query("{ 'expiry': { $lt: ?0 } }")
    List<Reservation> findExpiredReservations(LocalDateTime expiryTime);

    /**
     * Query MongoDB for reservations by bike station ID
     * 
     * @param bikeStationId The ID of the bike station
     * @return List of reservations at the specified station
     */
    List<Reservation> findByBikeStationId(UUID bikeStationId);

    List<Reservation> findByExpiryBefore(LocalDateTime time, ReservationStatus status);
}
