package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.events.TripEventDto;

public interface INotificationService {
    void publishTripEvent(TripEventDto event);
}
