package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.TripAssembler;
import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripPeriod;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.entities.records.YearWeek;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IPriceService;
import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
            UUID bikeStationEndId,
            int discountPercentage) {
        Trip trip = getTripById(tripId);
        LocalDateTime endTime = LocalDateTime.now();
        if (trip != null && trip.getStatus() == TripStatus.ONGOING) {
            trip.setBikeStationEndId(bikeStationEndId);
            trip.setEndDate(endTime);
            trip.setStatus(TripStatus.COMPLETED);
            
            double totalCost = priceService.calculatePrice(trip.getStartDate(), endTime, trip.getBikeType(),
                    trip.getPricingPlan(), discountPercentage);
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

    @Override
    public List<Trip> getCompletedTripsPastYearByUserId(UUID userId) {
        List<Trip> trips = tripRepository.findByUserIdAndStatus(userId, TripStatus.COMPLETED);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        return trips.stream()
                .filter(trip -> trip.getEndDate() != null && trip.getEndDate().isAfter(oneYearAgo))
                .toList();
    }
    @Override
    public boolean meetsMonthlyTripRequirement(UUID userId, int minTripsPerMonth, int months) {

        List<Trip> trips = tripRepository.findByUserId(userId);

        Map<YearMonth, Long> tripCounts = trips.stream()
                .filter(trip -> trip.getStartDate().isAfter(LocalDateTime.now().minusMonths(months)))
                .collect(Collectors.groupingBy(
                        trip -> YearMonth.from(trip.getStartDate()),
                        Collectors.counting()
                ));

        for (int i = 0; i < months; i++) {
            YearMonth month = YearMonth.from(LocalDate.now().minusMonths(i));
            long count = tripCounts.getOrDefault(month, 0L);
            if (count < minTripsPerMonth) return false;
        }

        return true;
    }

    @Override
    public boolean meetsWeeklyTripRequirement(UUID userId, int minTripsPerWeek, int months) {

        List<Trip> trips = tripRepository.findByUserId(userId);

        // compute start date
        LocalDateTime start = LocalDateTime.now().minusMonths(months);

        int totalWeeks = computeWeeksBetween(start, LocalDateTime.now());

        Map<YearWeek, Long> tripCounts = trips.stream()
                .filter(trip -> trip.getStartDate().isAfter(start))
                .collect(Collectors.groupingBy(
                        trip -> YearWeek.from(trip.getStartDate()),
                        Collectors.counting()
                ));

        for (int i = 0; i < totalWeeks; i++) {
            YearWeek yw = YearWeek.from(LocalDate.now().minusWeeks(i));
            long count = tripCounts.getOrDefault(yw, 0L);
            if (count < minTripsPerWeek) return false;
        }

        return true;
    }

    private int computeWeeksBetween(LocalDateTime start, LocalDateTime end) {

        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        startDate = startDate.with(WeekFields.ISO.getFirstDayOfWeek());
        endDate = endDate.with(WeekFields.ISO.getFirstDayOfWeek());

        int weeks = 0;

        while (!startDate.isAfter(endDate)) {
            weeks++;
            startDate = startDate.plusWeeks(1);
        }

        return weeks;
    }






}
