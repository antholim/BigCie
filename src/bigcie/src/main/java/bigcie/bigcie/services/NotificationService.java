package bigcie.bigcie.services;

import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import bigcie.bigcie.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.UUID;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.dtos.events.BikeStatusChangeDTO;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.dtos.events.DockStatusDTO;
import bigcie.bigcie.dtos.events.ReservationChangeDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String OPERATOR_DESTINATION = "/topic/operator-events";
    private static final String USER_DESTINATION = "/topic/user-events";

    @Override
    public void publishTripEvent(TripEventDto event) {
        log.debug("Publishing trip event {} for bike {}", event.type(), event.bikeId());
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION, event);
    }

    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        log.info("Sending heartbeat to operators");
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION, new TripEventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TripEventDto.TripEventType.TRIP_STARTED,
                OffsetDateTime.now()));
    }

    @Override
    public void notifyBikeStatusChange(UUID bikeId, BikeStatus newStatus) {
        log.debug("Notifying bike {} status change to {}", bikeId, newStatus);
        // Here you can implement the logic to notify about bike status change
        // For example, sending a message to a specific topic
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION,
                new BikeStatusChangeDTO(bikeId.toString(), newStatus.name()));
    }

    @Override
    public void notifyBikeStationStatusChange(UUID stationId, BikeStationStatus newStatus) {
        log.debug("Notifying bike station {} status change to {}", stationId, newStatus);
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION,
                new DockStatusDTO(stationId.toString(), newStatus.name()));
    }

    @Override
    public void notifyReservationChange(UUID reservationId, String newStatus) {
        log.debug("Notifying reservation {} status change to {}", reservationId, newStatus);
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION,
                new ReservationChangeDTO(reservationId.toString(), newStatus));
    }

    @Override
    public void notifyUserLoyaltyStatusChange(UUID userId, LoyaltyTier newLoyaltyTier) {
        log.info("Notifying user {} loyalty tier change to {}", userId, newLoyaltyTier);
        messagingTemplate.convertAndSend(USER_DESTINATION, newLoyaltyTier);
        // Implement notification logic here
    }

}
