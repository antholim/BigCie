package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.TripAssembler;
import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IPriceService;
import bigcie.bigcie.services.interfaces.ITripService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TripService implements ITripService {
    private final TripRepository tripRepository;
    private final IPriceService priceService;
    private final TripAssembler tripAssembler;

    public TripService(TripRepository tripRepository, IPriceService priceService, TripAssembler tripAssembler) {
        this.tripRepository = tripRepository;
        this.priceService = priceService;
        this.tripAssembler = tripAssembler;
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
            BikeType bikeType,
            UUID paymentInfoId

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
                .paymentInfoId(paymentInfoId)
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

    @Override
    public List<TripDto> getTripByUserId(UUID userId) {
        List<Trip> tripList = tripRepository.findByUserId(userId);
        return tripAssembler.enrichTripDtoList(tripList, userId);
    }
}
