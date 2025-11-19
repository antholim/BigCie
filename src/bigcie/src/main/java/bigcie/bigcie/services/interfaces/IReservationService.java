package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;

import java.util.List;
import java.util.UUID;

public interface IReservationService {

    Reservation createReservation(UUID userId, UUID stationId);

    Reservation getReservationById(UUID reservationId);

    List<Reservation> getReservationsByUserId(UUID userId);

    List<Reservation> getReservationsByBikeStation(UUID bikeStationId);

    void cancelReservation(UUID reservationId);

    boolean isReservationExpired(UUID reservationId);

    Reservation extendReservation(UUID reservationId, int additionalMinutes);

    List<Reservation> getAllActiveReservations();

    Reservation updateReservation(UUID reservationId, Reservation updatedReservation);

    void deleteReservation(UUID reservationId);

    List<Reservation> getAllReservations();
    List<Reservation> getAllActiveReservationsForUser(UUID userId);
    List<Reservation> getExpiredReservationsPastYearByUserId(UUID userId);


//    public void cleanupExpiredReservations();
    List<Reservation> getReservationsPastYearByUserIdAndStatus(UUID userId, ReservationStatus status);
}
