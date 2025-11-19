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
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TripService implements ITripService {
    private final TripRepository tripRepository;
    private final IPriceService priceService;
    private final TripAssembler tripAssembler;
    private final IFlexDollarService flexDollarService;

    public TripService(TripRepository tripRepository, IPriceService priceService, TripAssembler tripAssembler,
            IFlexDollarService flexDollarService) {
        this.tripRepository = tripRepository;
        this.priceService = priceService;
        this.tripAssembler = tripAssembler;
        this.flexDollarService = flexDollarService;
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
            UUID bikeStationEndId) {
        Trip trip = getTripById(tripId);
        LocalDateTime endTime = LocalDateTime.now();
        if (trip != null && trip.getStatus() == TripStatus.ONGOING) {
            trip.setBikeStationEndId(bikeStationEndId);
            trip.setEndDate(endTime);
            trip.setStatus(TripStatus.COMPLETED);
            
            double totalCost = priceService.calculatePrice(trip.getStartDate(), endTime, trip.getBikeType(),
                    trip.getPricingPlan());
            trip.setCost(totalCost);
            
            // Auto-apply flex dollars
            double flexDollarsDeducted = flexDollarService.deductFlexDollars(trip.getUserId(), totalCost);
            trip.setFlexDollarsUsed(flexDollarsDeducted);
            trip.setAmountCharged(totalCost - flexDollarsDeducted);
            
            log.info("Trip {} completed. Total: ${}, Flex: ${}, Charged: ${}", 
                    tripId, totalCost, flexDollarsDeducted, trip.getAmountCharged());
            
            tripRepository.save(trip);
        } else {
            throw new IllegalArgumentException("Trip not found or already completed");
        }
    }

    @Override
    public Page<TripDto> getTripByUserId(UUID userId, Pageable pageable) {
        Page<Trip> tripList = tripRepository.findByUserId(userId, pageable);
        for (Trip trip : tripList) {
            System.out.println("Trip ID: " + trip.getId() + ", Start Date: " + trip.getStartDate() +
                    ", End Date: " + trip.getEndDate() + ", Status: " + trip.getStatus());
        }
        return tripAssembler.enrichTripDtoPage(tripList, userId);
    }
    @Override
    public List<TripDto> getTripByUserId(UUID userId) {
        List<Trip> tripList = tripRepository.findByUserId(userId);
        for (Trip trip : tripList) {
            System.out.println("Trip ID: " + trip.getId() + ", Start Date: " + trip.getStartDate() +
                    ", End Date: " + trip.getEndDate() + ", Status: " + trip.getStatus());
        }
        return tripAssembler.enrichTripDtoList(tripList, userId);
    }

    // get all trips - for admin purposes
    @Override
    public List<TripDto> getAllTrips() {
        List<Trip> tripList = tripRepository.findAll();
        return tripAssembler.enrichTripDtoListNoLogging(tripList);
    }

}
