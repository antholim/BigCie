package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;

import java.util.List;
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
    List<TripDto> getTripByUserId(UUID userId);
}
