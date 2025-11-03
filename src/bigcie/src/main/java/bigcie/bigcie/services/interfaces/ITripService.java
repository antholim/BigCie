package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.Trip;

import java.util.UUID;

public interface ITripService {
    Trip getTripById(UUID id);
    Trip createTrip(
            UUID userId,
            UUID bikeId,
            UUID bikeStationStartId
    );
    void endTrip(
            UUID tripId,
            UUID bikeStationEndId
    );
}
