package bigcie.bigcie.services;

import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String OPERATOR_DESTINATION = "/topic/operator-events";

    @Override
    public void publishTripEvent(TripEventDto event) {
        log.debug("Publishing trip event {} for bike {}", event.type(), event.bikeId());
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION, event);
    }

    @Scheduled(fixedRate = 1000)
    public void sendHeartbeat() {
        log.info("Sending heartbeat to operators");
        messagingTemplate.convertAndSend(OPERATOR_DESTINATION, new TripEventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TripEventDto.TripEventType.TRIP_STARTED,
                OffsetDateTime.now()
                ));
    }
}
