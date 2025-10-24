package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.events.TripEventDto;

public interface INotificationService {
    void publishTripEvent(TripEventDto event);

    void notifyBikeStatusChange(java.util.UUID bikeId, bigcie.bigcie.entities.enums.BikeStatus newStatus);

    void notifyBikeStationStatusChange(java.util.UUID stationId,
            bigcie.bigcie.entities.enums.BikeStationStatus newStatus);

    void notifyReservationChange(java.util.UUID reservationId, String newStatus);
}
