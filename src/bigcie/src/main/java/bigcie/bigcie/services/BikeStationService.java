package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import bigcie.bigcie.services.interfaces.INotificationService;

import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.repositories.ReservationRepository;
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

    public BikeStationService(BikeStationRepository bikeStationRepository, ReservationRepository reservationRepository,
                              BikeRepository bikeRepository, INotificationService notificationService, IUserService userService, ITripService tripService) {
        this.userService = userService;
        this.bikeStationRepository = bikeStationRepository;
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.notificationService = notificationService;
        this.tripService = tripService;
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
        bikeStationEntity.setNumberOfBikesDocked(0);
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
    public BikeStation updateStation(UUID id, BikeStation station) {
        BikeStation existingStation = getStationById(id);
        existingStation.setName(station.getName());
        existingStation.setStatus(station.getStatus());
        existingStation.setLatitude(station.getLatitude());
        existingStation.setLongitude(station.getLongitude());
        existingStation.setAddress(station.getAddress());
        existingStation.setCapacity(station.getCapacity());
        existingStation.setNumberOfBikesDocked(station.getNumberOfBikesDocked());
        // use bikesIds (UUID list) instead of embedded Bike objects
        existingStation.setBikesIds(station.getBikesIds());
        existingStation.setReservationHoldTimeMinutes(station.getReservationHoldTimeMinutes());
        return bikeStationRepository.save(existingStation);
    }

    @Override
    public void deleteStation(UUID id) {
        bikeStationRepository.deleteById(id);
    }

    @Override
    public BikeStation updateStationStatus(UUID id, BikeStationStatus status) {

        BikeStation station = getStationById(id);

        station.setStatus(status);
        return bikeStationRepository.save(station);
    }

    @Override
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
        station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() + 1);
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.AVAILABLE);
        bike.setStatus(BikeStatus.AVAILABLE);
        bikeRepository.save(bike);

        if (station.getStatus() == BikeStationStatus.OCCUPIED && station.getNumberOfBikesDocked() == station.getCapacity()) {
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
            rider.getCurrentBikes().remove(bike.getId());
            userService.updateUser(rider);
            tripService.endTrip(
                    rider.getActiveTripId(),
                    stationId
            );
        }

        bikeStationRepository.save(station);

    }

    @Override
    public UUID undockBike(UUID stationId, UUID userId) {
        BikeStation station = getStationById(stationId);
        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Station is out of service");
        }
        List<Reservation> list = reservationRepository.findByUserId(userId);
        for (Reservation reservation : list) {
            if (reservation.getBikeStationId() != null && reservation.getBikeStationId().equals(stationId)) {
                Bike bike = bikeRepository.findBikeById(reservation.getBikeId());
                notificationService.notifyReservationChange(reservation.getId(), "CANCELLED");
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(reservation);
            }
        }
        // To undock a bike, the bike needs to be of status "AVAILABLE"
        // check if all bike are reserved
        List<Bike> stationBikes = getStationBikes(stationId);
        if (stationBikes.isEmpty()) {
            throw new IllegalStateException("No bikes at station");
        }
        if (stationBikes.stream().allMatch(b -> b.getStatus() == BikeStatus.RESERVED)) {
            throw new IllegalStateException("All bikes are reserved");
        }
        if (!hasAvailableBikes(stationId)) {
            throw new IllegalStateException("No available bikes to undock");
        }

        Bike bike = stationBikes.stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available bikes found"));

        // remove bike id from station
        station.getBikesIds().remove(bike.getId());
        station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() - 1);
        bikeStationRepository.save(station);

        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.ON_TRIP);
        bike.setStatus(BikeStatus.ON_TRIP);

        User user = userService.getUserByUUID(userId);
        if (user instanceof Rider rider) {
            rider.getCurrentBikes().add(bike.getId());
            userService.updateUser(rider);
        }

        if (station.getNumberOfBikesDocked() == 0) {
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.EMPTY);
            station.setStatus(BikeStationStatus.EMPTY);
            bikeStationRepository.save(station);
        }
        bikeRepository.save(bike);
        Trip trip = tripService.createTrip(
                userId,
                bike.getId(),
                stationId
        );
        if (user instanceof Rider rider) {
            rider.setActiveTripId(trip.getId());
            userService.updateUser(rider);
        }
        return bike.getId();
    }

    @Override
    public boolean hasAvailableDocks(UUID stationId) {
        BikeStation station = getStationById(stationId);
        return station.getNumberOfBikesDocked() < station.getCapacity();
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
        return station.getNumberOfBikesDocked() == 0;
    }

    @Override
    public void holdBike(UUID stationId) {
        BikeStation station = getStationById(stationId);
        if (!hasAvailableBikes(stationId)) {
            throw new IllegalStateException("No available bikes to hold");
        }
        // Find the first available bike
        Bike bike = getStationBikes(stationId).stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available bikes found"));

        // Create a reservation
        Reservation reservation = new Reservation(UUID.randomUUID(), UUID.randomUUID(), stationId, bike.getId(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(station.getReservationHoldTimeMinutes()), ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        // Update bike status to RESERVED
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.RESERVED);
        bike.setStatus(BikeStatus.RESERVED);

        bikeRepository.save(bike);

    }
    @Override
    public List<Bike> getStationBikes(UUID stationId) {
        BikeStation station = bikeStationRepository.findById(stationId)
                .orElseThrow(() -> new NoSuchElementException("station not found"));
        List<UUID> bikeIds = station.getBikesIds();
        List<Bike> bikes = StreamSupport.stream(bikeRepository.findAllById(bikeIds).spliterator(), false)
                .collect(Collectors.toList());
        return bikes;
    }

}
