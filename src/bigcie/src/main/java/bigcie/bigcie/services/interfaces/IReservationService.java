package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.ReservationRequest.ReservationRequest;
import bigcie.bigcie.entities.Reservation;

import java.util.List;
import java.util.UUID;

public interface IReservationService {

    public ReservationRequest createReservation(UUID userId, UUID stationId);

    public Reservation getReservationById(UUID reservationId);

    public List<Reservation> getReservationsByUserId(UUID userId);

    public List<Reservation> getReservationsByBikeStation(UUID bikeStationId);

    public void cancelReservation(UUID reservationId);

    public boolean isReservationExpired(UUID reservationId);

    public Reservation extendReservation(UUID reservationId, int additionalMinutes);

    public List<Reservation> getAllActiveReservations();

    public Reservation updateReservation(UUID reservationId, Reservation updatedReservation);

    public void deleteReservation(UUID reservationId);

    public List<Reservation> getAllReservations();

    public void cleanupExpiredReservations();
}
