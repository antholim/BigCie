package bigcie.bigcie.services;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.repositories.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class VerificationService {
    private final ReservationRepository reservationRepository;

    public VerificationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(fixedRate = 15000)
    public void checkExpiredReservation(){
        List<Reservation> expired = reservationRepository.findByExpiryBefore(LocalDateTime.now(ZoneOffset.UTC));

        expired.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        });

    }
}
