package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ITripService {
        Trip getTripById(UUID id);

        Trip createTrip(
                        UUID userId,
                        UUID bikeId,
                        UUID bikeStationStartId,
                        PricingPlan pricingPlan,
                        BikeType bikeType,
                        UUID paymentInfoId

        );

        void endTrip(
                        UUID tripId,
                        UUID bikeStationEndId,
                        int discount);

        List<TripDto> getTripByUserId(UUID userId);

        // get all trips - for admin purposes
        List<TripDto> getAllTrips();

        Page<TripDto> getTripByUserId(UUID userId, Pageable pageable);

        List<Trip> getCompletedTripsPastYearByUserId(UUID userId);

        boolean meetsMonthlyTripRequirement(UUID userId, int minTripsPerMonth, int months);

        boolean meetsWeeklyTripRequirement(UUID userId, int minTripsPerWeek, int months);
}
