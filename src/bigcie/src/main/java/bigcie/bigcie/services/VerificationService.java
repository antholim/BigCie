package bigcie.bigcie.services;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.repositories.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VerificationService {
    private final ReservationRepository reservationRepository;

    public VerificationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Scheduled(fixedRate = 15000)
    public void checkExpiredReservation(){
        log.info("Checking expired reservation");
        List<Reservation> expired = reservationRepository.findByExpiryBefore(LocalDateTime.now(), ReservationStatus.ACTIVE);

        expired.forEach(reservation -> {
            log.info(reservation.getExpiry().toString());
            log.info(LocalDateTime.now().toString());
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Reservation {} has expired and status updated to EXPIRED", reservation.getId());
        });

    }
}
