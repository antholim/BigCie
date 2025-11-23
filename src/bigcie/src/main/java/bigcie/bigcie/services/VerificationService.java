package bigcie.bigcie.services;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
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
    private final LoyaltyTierContext loyaltyTierContext;

    public VerificationService(ReservationRepository reservationRepository, LoyaltyTierContext loyaltyTierContext) {
        this.reservationRepository = reservationRepository;
        this.loyaltyTierContext = loyaltyTierContext;
    }

    @Scheduled(fixedRate = 15000)
    public void checkExpiredReservation(){
        log.info("Checking expired reservation");
        List<Reservation> expired = reservationRepository.findExpiredActiveReservations(LocalDateTime.now());

        expired.forEach(reservation -> {
            System.out.println(expired.toString());
        });

            expired.forEach(reservation -> {
            log.info(reservation.getExpiry().toString());
            log.info(LocalDateTime.now().toString());
            reservation.setStatus(ReservationStatus.EXPIRED);
            loyaltyTierContext.evaluateUserTierUpgrade(reservation.getUserId());
            reservationRepository.save(reservation);
            log.info("Reservation {} has expired and status updated to EXPIRED", reservation.getId());
        });

    }
    @Scheduled(fixedRate = 15000)
    public void checkExpiredPlans(){

    }
}
