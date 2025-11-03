package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;

import java.util.UUID;

public interface ITripService {
    Trip getTripById(UUID id);
    Trip createTrip(
            UUID userId,
            UUID bikeId,
            UUID bikeStationStartId,
            PricingPlan pricingPlan,
            BikeType bikeType

    );
    void endTrip(
            UUID tripId,
            UUID bikeStationEndId
    );
}
