package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.Trip;

import java.util.UUID;

public interface ITripService {
    Trip getTripById(UUID id);
    void createTrip(
            UUID userId,
            UUID bikeId,
            UUID bikeStationStartId,
            UUID bikeStationEndId,
            double distanceInKm,
            double cost
    );
}
