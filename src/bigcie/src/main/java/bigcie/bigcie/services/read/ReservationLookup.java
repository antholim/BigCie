package bigcie.bigcie.services.read;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.repositories.ReservationRepository;
import bigcie.bigcie.services.read.interfaces.IReservationLookup;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationLookup implements IReservationLookup {
    private final ReservationRepository reservationRepository;

    public ReservationLookup(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> getExpiredReservationsPastYearByUserId(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.EXPIRED);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        return reservations.stream()
                .filter(reservation -> reservation.getExpiry().isAfter(oneYearAgo))
                .toList();
    }

    @Override
    public List<Reservation> getReservationsPastYearByUserIdAndStatus(UUID userId, ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, status);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        return reservations.stream()
                .filter(reservation -> reservation.getExpiry().isAfter(oneYearAgo))
                .toList();
    }
}
