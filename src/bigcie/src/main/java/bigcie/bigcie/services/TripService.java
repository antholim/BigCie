package bigcie.bigcie.services;

import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IPriceService;
import bigcie.bigcie.services.interfaces.ITripService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TripService implements ITripService {
    private final TripRepository tripRepository;
    private final IPriceService priceService;

    public TripService(TripRepository tripRepository, IPriceService priceService) {
        this.tripRepository = tripRepository;
        this.priceService = priceService;
    }

    @Override
    public Trip getTripById(UUID id) {
        return tripRepository.findById(id).orElse(null);
    }

    @Override
    public Trip createTrip(
            UUID userId,
            UUID bikeId,
            UUID bikeStationStartId,
            PricingPlan pricingPlan,
            BikeType bikeType

    ) {
        Trip trip = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .bikeId(bikeId)
                .bikeStationStartId(bikeStationStartId)
                .startDate(LocalDateTime.now())
                .status(TripStatus.ONGOING)
                .pricingPlan(pricingPlan)
                .bikeType(bikeType)
                .build();

        tripRepository.save(trip);
        return trip;
    }

    public void endTrip(
            UUID tripId,
            UUID bikeStationEndId
    ) {
        Trip trip = getTripById(tripId);
        LocalDateTime endTime = LocalDateTime.now();
        if (trip != null && trip.getStatus() == TripStatus.ONGOING) {
            trip.setBikeStationEndId(bikeStationEndId);
            trip.setEndDate(endTime.plusMinutes(5));
            trip.setStatus(TripStatus.COMPLETED);
            trip.setCost(priceService.calculatePrice(trip.getStartDate(), endTime, trip.getBikeType(), trip.getPricingPlan()));
            tripRepository.save(trip);
        } else {
            throw new IllegalArgumentException("Trip not found or already completed");
        }
    }



}
