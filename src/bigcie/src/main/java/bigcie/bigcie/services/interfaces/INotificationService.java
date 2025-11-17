package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;

import java.util.UUID;

public interface INotificationService {
    void publishTripEvent(TripEventDto event);

    void notifyBikeStatusChange(UUID bikeId, bigcie.bigcie.entities.enums.BikeStatus newStatus);

    void notifyBikeStationStatusChange(UUID stationId,
            bigcie.bigcie.entities.enums.BikeStationStatus newStatus);

    void notifyReservationChange(UUID reservationId, String newStatus);
    void notifyUserLoyaltyStatusChange(UUID userId, LoyaltyTier newLoyaltyTier);

}
