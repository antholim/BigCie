package bigcie.bigcie.services.read.interfaces;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;

import java.util.List;
import java.util.UUID;

public interface IReservationLookup {
    List<Reservation> getExpiredReservationsPastYearByUserId(UUID userId);
    List<Reservation> getReservationsPastYearByUserIdAndStatus(UUID userId, ReservationStatus status);
}
