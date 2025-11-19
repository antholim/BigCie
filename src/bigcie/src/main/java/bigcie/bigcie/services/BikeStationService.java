package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.dtos.BikeStationRequest.MoveBikeRequest;
import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.exceptions.*;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import bigcie.bigcie.services.interfaces.INotificationService;

import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IUserService;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import bigcie.bigcie.constants.prices.Prices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.repositories.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class BikeStationService implements IBikeStationService {

    private final BikeRepository bikeRepository;
    private final BikeStationRepository bikeStationRepository;
    private final ReservationRepository reservationRepository;
    private final INotificationService notificationService;
    private final IUserService userService;
    private final ITripService tripService;
    private final IFlexDollarService flexDollarService;
    private final LoyaltyTierContext loyaltyTierContext;

    public BikeStationService(BikeStationRepository bikeStationRepository, ReservationRepository reservationRepository,
            BikeRepository bikeRepository, INotificationService notificationService, IUserService userService,
            ITripService tripService, IFlexDollarService flexDollarService, LoyaltyTierContext loyaltyTierContext) {
        this.userService = userService;
        this.bikeStationRepository = bikeStationRepository;
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.notificationService = notificationService;
        this.tripService = tripService;
        this.flexDollarService = flexDollarService;
        this.loyaltyTierContext = loyaltyTierContext;
    }

    @Override
    public BikeStation createStation(BikeStationRequest station) {
        BikeStation bikeStationEntity = new BikeStation();
        bikeStationEntity.setId(UUID.randomUUID());
        bikeStationEntity.setName(station.getName());
        bikeStationEntity.setStatus(station.getStatus());
        bikeStationEntity.setLatitude(station.getLatitude());
        bikeStationEntity.setLongitude(station.getLongitude());
        bikeStationEntity.setAddress(station.getAddress());
        bikeStationEntity.setCapacity(station.getCapacity());
        bikeStationEntity.setReservationHoldTimeMinutes(station.getReservationHoldTimeMinutes());
        // bikeStationEntity.setNumberOfBikesDocked(0);
        bikeStationEntity.setStandardBikesDocked(0);
        bikeStationEntity.setEBikesDocked(0);
        return bikeStationRepository.save(bikeStationEntity);
    }

    @Override
    public BikeStation getStationById(UUID id) {
        return bikeStationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
    }

    @Override
    public List<BikeStation> getAllStations() {
        return bikeStationRepository.findAll();
    }

    @Override
    public List<BikeStation> getStationsByStatus(BikeStationStatus status) {
        return bikeStationRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public BikeStation updateStation(UUID id, BikeStation station) {
        BikeStation existingStation = getStationById(id);
        existingStation.setName(station.getName());
        existingStation.setStatus(station.getStatus());
        existingStation.setLatitude(station.getLatitude());
        existingStation.setLongitude(station.getLongitude());
        existingStation.setAddress(station.getAddress());
        existingStation.setCapacity(station.getCapacity());
        existingStation.setEBikesDocked(station.getEBikesDocked());
        existingStation.setStandardBikesDocked(station.getStandardBikesDocked());
        // existingStation.setNumberOfBikesDocked(station.getNumberOfBikesDocked());
        // use bikesIds (UUID list) instead of embedded Bike objects
        existingStation.setBikesIds(station.getBikesIds());
        existingStation.setReservationHoldTimeMinutes(station.getReservationHoldTimeMinutes());
        return bikeStationRepository.save(existingStation);
    }

    @Override
    @Transactional
    public void deleteStation(UUID id) {
        bikeStationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BikeStation updateStationStatus(UUID id, BikeStationStatus status) {

        BikeStation station = getStationById(id);

        station.setStatus(status);
        return bikeStationRepository.save(station);
    }

    @Override
    @Transactional
    public void dockBike(UUID stationId, UUID bikeId, UUID userId) {

        BikeStation station = getStationById(stationId);

        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Station is out of service");
        }

        if (!hasAvailableDocks(stationId)) {
            throw new IllegalStateException("No available docks");
        }

        log.info("Docking bike {} to station {}", bikeId, stationId);
        System.out.println("Docking bike " + bikeId.toString() + " to station " + stationId.toString());
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found"));
        if (bike.getStatus() != BikeStatus.ON_TRIP) {
            throw new IllegalStateException("Bike is not on trip");
        }
        // store only the bike id on the station
        station.getBikesIds().add(bike.getId());
        switch (bike.getBikeType()) {
            case STANDARD -> {
                station.setStandardBikesDocked(station.getStandardBikesDocked() + 1);
            }
            case E_BIKE -> {
                station.setEBikesDocked(station.getEBikesDocked() + 1);
            }
        }
        // station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() + 1);
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.AVAILABLE);
        bike.setStatus(BikeStatus.AVAILABLE);
        bikeRepository.save(bike);

        if (station.getStatus() == BikeStationStatus.OCCUPIED
                && (station.getEBikesDocked() + station.getStandardBikesDocked()) == station.getCapacity()) {
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.FULL);
            station.setStatus(BikeStationStatus.FULL);
        } else if (station.getStatus() == BikeStationStatus.EMPTY) {
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.OCCUPIED);
            station.setStatus(BikeStationStatus.OCCUPIED);
        }

        User user = userService.getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
            UUID activeTripId = rider.getActiveTripId() != null ? rider.getActiveTripId().getFirst() : null;
            
            rider.getCurrentBikes().remove(bike.getId());
            rider.setActiveTripId(null);
            userService.updateUser(rider);
            
            // End trip after updating rider to avoid overwriting flex dollar deductions
            if (activeTripId != null) {
                tripService.endTrip(activeTripId, stationId, rider.getLoyaltyTier().getDiscountPercentage());
            }
            
            loyaltyTierContext.evaluateUserTierUpgrade(rider);
        }

        bikeStationRepository.save(station);

        // Award flex dollars if station capacity is below minimum threshold
        int totalBikesDocked = station.getStandardBikesDocked() + station.getEBikesDocked();
        double capacityPercentage = (double) totalBikesDocked / station.getCapacity();
        
        if (capacityPercentage < Prices.MIN_CAPACITY_THRESHOLD) {
            flexDollarService.addFlexDollars(userId, Prices.FLEX_DOLLAR_REWARD);
            log.info("Awarded {} flex dollars to user {} for docking at low-capacity station {} ({}% capacity)", 
                    Prices.FLEX_DOLLAR_REWARD, userId, stationId, capacityPercentage * 100);
        }

    }

    @Override
    @Transactional
    public UUID undockBike(UUID stationId, UUID userId, BikeType bikeType) {
        BikeStation station = getStationById(stationId);
        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
            throw new StationOutOfServiceException();
        }
        User user = userService.getUserByUUID(userId);
        if (!(user instanceof Rider rider)) {
            throw new UserIsNotRiderException();
        }
        if (rider.getDefaultPaymentInfo() == null) {
            throw new NoDefaultPaymentMethodFoundException();
        }
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        for (Reservation reservation : reservations) {
            if (stationId.equals(reservation.getBikeStationId())) {
                Bike reservedBike = bikeRepository.findBikeById(reservation.getBikeId());
                notificationService.notifyReservationChange(reservation.getId(), "CANCELLED");
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(reservation);
            }
        }

        List<Bike> stationBikes = getStationBikes(stationId);
        if (stationBikes.isEmpty()) {
            throw new StationIsEmptyException();
        }
        if (stationBikes.stream().allMatch(b -> b.getStatus() == BikeStatus.RESERVED)) {
            throw new AllBikesReservedException();
        }
        if (!rider.getCurrentBikes().isEmpty()) {
            throw new RiderAlreadyHasBikeException();
        }
        Bike bike = stationBikes.stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE && b.getBikeType() == bikeType)
                .findFirst()
                .orElseThrow(NoAvailableBikesOfRequestedTypeException::new);

        station.getBikesIds().remove(bike.getId());
        switch (bike.getBikeType()) {
            case STANDARD -> {
                station.setStandardBikesDocked(station.getStandardBikesDocked() - 1);
            }
            case E_BIKE -> {
                station.setEBikesDocked(station.getEBikesDocked() - 1);
            }
        }
        // station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() - 1);
        if (station.getStandardBikesDocked() + station.getEBikesDocked() == 0) {
            station.setStatus(BikeStationStatus.EMPTY);
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.EMPTY);
        }
        bikeStationRepository.save(station);

        bike.setStatus(BikeStatus.ON_TRIP);
        bikeRepository.save(bike);
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.ON_TRIP);


        rider.getCurrentBikes().add(bike.getId());
        userService.updateUser(rider);

        Trip trip = tripService.createTrip(
                userId,
                bike.getId(),
                stationId,
                rider.getPricingPlanInformation().getPricingPlan(),
                bike.getBikeType(),
                rider.getDefaultPaymentInfo().getId());
        rider.getActiveTripId().add(trip.getId());

//        loyaltyTierContext.evaluateUserTierUpgrade(rider);
        userService.updateUser(rider);

        return bike.getId();
    }

    @Override
    public boolean hasAvailableDocks(UUID stationId) {
        BikeStation station = getStationById(stationId);
        return (station.getStandardBikesDocked() + station.getEBikesDocked()) < station.getCapacity();
    }

    // Different from isempty cuz bikes can be docked but reserved
    @Override
    public boolean hasAvailableBikes(UUID stationId) {
        List<Bike> stationBikes = getStationBikes(stationId);
        for (var bike : stationBikes) {
            if (!bike.getStatus().equals(BikeStatus.RESERVED)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty(UUID stationId) {
        BikeStation station = getStationById(stationId);
        return (station.getStandardBikesDocked() + station.getEBikesDocked()) == 0;
    }

    @Override
    @Transactional
    public void holdBike(UUID stationId) {
        BikeStation station = getStationById(stationId);
        if (!hasAvailableBikes(stationId)) {
            throw new IllegalStateException("No available bikes to hold");
        }
        // Find the first available bike
        Bike bike = getStationBikes(stationId).stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(StationIsEmptyException::new);

        // Create a reservation
        Reservation reservation = new Reservation(UUID.randomUUID(), UUID.randomUUID(), stationId, bike.getId(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(station.getReservationHoldTimeMinutes()),
                ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        // Update bike status to RESERVED
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.RESERVED);
        bike.setStatus(BikeStatus.RESERVED);

        bikeRepository.save(bike);

    }

    @Override
    public List<Bike> getStationBikes(UUID stationId) {
        BikeStation station = bikeStationRepository.findById(stationId)
                .orElseThrow(StationNotFoundException::new);
        List<UUID> bikeIds = station.getBikesIds();
        List<Bike> bikes = StreamSupport.stream(bikeRepository.findAllById(bikeIds).spliterator(), false)
                .collect(Collectors.toList());
        return bikes;
    }

    @Override
    public String getStationNameById(UUID id) {
        BikeStation station = bikeStationRepository.findById(id)
                .orElseThrow(StationNotFoundException::new);
        if (station != null) {
            return station.getName();
        }
        return "";
    }

    @Transactional
    @Override
    public void moveBike(MoveBikeRequest moveBikeRequest) {
        UUID sourceStationId = moveBikeRequest.getSourceStationId();
        UUID destinationStationId = moveBikeRequest.getDestinationStationId();
        BikeStation sourceStation = getStationById(sourceStationId);
        BikeStation destinationStation = getStationById(destinationStationId);

        if (sourceStation.equals(destinationStation)) {
            throw new SourceAndTargetStationAreEqualsException();
        }
        if (!hasAvailableBikes(sourceStationId)) {
            throw new StationIsEmptyException();
        }
        Bike bike = getStationBikes(sourceStationId).stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(StationIsFullException::new);

        sourceStation.getBikesIds().remove(bike.getId());
        switch (bike.getBikeType()) {
            case STANDARD -> {
                sourceStation.setStandardBikesDocked(sourceStation.getStandardBikesDocked() - 1);
            }
            case E_BIKE -> {
                sourceStation.setEBikesDocked(sourceStation.getEBikesDocked() - 1);
            }
        }
        if (sourceStation.getStandardBikesDocked() + sourceStation.getEBikesDocked() == 0) {
            sourceStation.setStatus(BikeStationStatus.EMPTY);
            notificationService.notifyBikeStationStatusChange(sourceStation.getId(), BikeStationStatus.EMPTY);
        }
        bikeStationRepository.save(sourceStation);


        destinationStation.getBikesIds().add(bike.getId());
        switch (bike.getBikeType()) {
            case STANDARD -> {
                destinationStation.setStandardBikesDocked(destinationStation.getStandardBikesDocked() + 1);
            }
            case E_BIKE -> {
                destinationStation.setEBikesDocked(destinationStation.getEBikesDocked() + 1);
            }
        }
        if (destinationStation.getStatus() == BikeStationStatus.OCCUPIED
                && (destinationStation.getEBikesDocked() + destinationStation.getStandardBikesDocked()) == destinationStation.getCapacity()) {
            notificationService.notifyBikeStationStatusChange(destinationStation.getId(), BikeStationStatus.FULL);
            destinationStation.setStatus(BikeStationStatus.FULL);
        }
        bikeStationRepository.save(destinationStation);

    }

    @Transactional
    @Override
    public void rebalanceBikes() {
        // Placeholder for bike rebalancing logic
        log.info("Rebalancing bikes across stations...");
        List<BikeStation> stations = bikeStationRepository.findAll();
        // Implement rebalancing algorithm here
        int totalBikes = stations.stream()
                .mapToInt(station -> station.getStandardBikesDocked() + station.getEBikesDocked())
                .sum();
        int average = totalBikes / stations.size(); // 2
        List<UUID> bikeIdsToMove = new ArrayList<>(); // 10

        for (BikeStation station : stations) {
            bikeIdsToMove.addAll(station.getBikesIds());
            station.getBikesIds().clear();
            station.setStandardBikesDocked(0);
            station.setEBikesDocked(0);
        }

        for (BikeStation station : stations) {
            int standardBikes = 0;
            int eBikes = 0;
            for (int i = 0; i < average; i++) {
                UUID bikeId = bikeIdsToMove.removeFirst();
                Bike bike = bikeRepository.findBikeById(bikeId);
                switch (bike.getBikeType()) {
                    case STANDARD -> {
                        standardBikes++;
                    }
                    case E_BIKE -> {
                        eBikes++;
                    }
                }
                station.getBikesIds().add(bikeId);
                station.setStandardBikesDocked(standardBikes);
                station.setEBikesDocked(eBikes);
            }
            if (station.getBikesIds().size() == 0) {
                station.setStatus(BikeStationStatus.EMPTY);
            } else if (station.getBikesIds().size() < station.getCapacity()) {
                station.setStatus(BikeStationStatus.OCCUPIED);
            } else {
                station.setStatus(BikeStationStatus.FULL);
            }
            bikeStationRepository.save(station);
        }
        if (!bikeIdsToMove.isEmpty()) {

            for (BikeStation station : stations) {
                int standardBikes = 0;
                int eBikes = 0;
                if (bikeIdsToMove.isEmpty()) {
                    break;
                }
                UUID bikeId = bikeIdsToMove.removeFirst();
                Bike bike = bikeRepository.findBikeById(bikeId);
                switch (bike.getBikeType()) {
                    case STANDARD -> {
                        standardBikes++;
                    }
                    case E_BIKE -> {
                        eBikes++;
                    }
                }
                station.getBikesIds().add(bikeId);
                station.setStandardBikesDocked(standardBikes);
                station.setEBikesDocked(eBikes);
                station.getBikesIds().add(bikeId);
                if (station.getBikesIds().size() == 0) {
                    station.setStatus(BikeStationStatus.EMPTY);
                } else if (station.getBikesIds().size() < station.getCapacity()) {
                    station.setStatus(BikeStationStatus.OCCUPIED);
                } else {
                    station.setStatus(BikeStationStatus.FULL);
                }
                bikeStationRepository.save(station);
            }
        }

    }

}
