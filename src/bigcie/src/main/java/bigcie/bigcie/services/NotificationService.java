package bigcie.bigcie.services;

import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
}
