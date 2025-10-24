package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import bigcie.bigcie.services.interfaces.INotificationService;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.repositories.ReservationRepository;
import java.time.LocalDateTime;

@Service
public class BikeStationService implements IBikeStationService {

    private final BikeRepository bikeRepository;
    private final BikeStationRepository bikeStationRepository;
    private final ReservationRepository reservationRepository;
    private final INotificationService notificationService;

    public BikeStationService(BikeStationRepository bikeStationRepository, ReservationRepository reservationRepository,
            BikeRepository bikeRepository, INotificationService notificationService) {
        this.bikeStationRepository = bikeStationRepository;
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.notificationService = notificationService;
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
        existingStation.setBikes(station.getBikes());
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
    public void dockBike(UUID stationId, UUID bikeId) {

        // To dock a bike, the bike needs to be of status "ON_TRIP"
        // Check if station is not full
        // Check if dock is not out of service
        BikeStation station = getStationById(stationId);

        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Station is out of service");
        }

        if (!hasAvailableDocks(stationId)) {
            throw new IllegalStateException("No available docks");
        }

        // Dock the bike
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found"));
        if (bike.getStatus() != BikeStatus.ON_TRIP) {
            throw new IllegalStateException("Bike is not on trip");
        }

        station.getBikes().add(bike);
        station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() + 1);
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.AVAILABLE);
        bike.setStatus(BikeStatus.AVAILABLE);
        bikeRepository.save(bike);

        // check if the bike is full
        if (station.getNumberOfBikesDocked() == station.getCapacity()) {
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.FULL);
            station.setStatus(BikeStationStatus.FULL);
        }

        bikeStationRepository.save(station);

    }

    @Override
    public UUID undockBike(UUID stationId) {
        // check if station is out of service
        BikeStation station = getStationById(stationId);
        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Station is out of service");
        }

        // To undock a bike, the bike needs to be of status "AVAILABLE"
        // check if all bike are reserved
        if (station.getBikes().stream().allMatch(b -> b.getStatus() == BikeStatus.RESERVED)) {
            throw new IllegalStateException("All bikes are reserved");
        }
        if (!hasAvailableBikes(stationId)) {
            throw new IllegalStateException("No available bikes to undock");
        }
        // Find the first available bike
        Bike bike = station.getBikes().stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available bikes found"));
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.AVAILABLE);
        bike.setStatus(BikeStatus.AVAILABLE);

        // make reservation null if any
        station.getBikes().remove(bike);
        station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() - 1);
        bikeStationRepository.save(station);

        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.ON_TRIP);
        bike.setStatus(BikeStatus.ON_TRIP);

        // check if reserved bike exists and remove reservation
        reservationRepository.findAllByBikeId(bike.getId()).forEach(reservation -> {
            notificationService.notifyReservationChange(reservation.getId(), "CANCELLED");
            reservationRepository.delete(reservation);
        });

        // check if empty
        if (station.getNumberOfBikesDocked() == 0) {
            notificationService.notifyBikeStationStatusChange(station.getId(), BikeStationStatus.EMPTY);
            station.setStatus(BikeStationStatus.EMPTY);
            bikeStationRepository.save(station);
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
        BikeStation station = getStationById(stationId);
        // Check for bikes that are not reserved
        for (var bike : station.getBikes()) {
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
        Bike bike = station.getBikes().stream()
                .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available bikes found"));

        // Create a reservation
        Reservation reservation = new Reservation(UUID.randomUUID(), UUID.randomUUID(), stationId, bike.getId(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(station.getReservationHoldTimeMinutes()));
        reservationRepository.save(reservation);

        // Update bike status to RESERVED
        notificationService.notifyBikeStatusChange(bike.getId(), BikeStatus.RESERVED);
        bike.setStatus(BikeStatus.RESERVED);

        bikeRepository.save(bike);

    }

    // @Override
    // public BikeStation dockBike(UUID stationId, UUID bikeId) {
    // BikeStation station = getStationById(stationId);
    //
    // // R-BMS-05: Block if out of service
    // if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
    // throw new IllegalStateException("Station is out of service");
    // }
    //
    // // R-BMS-02: Prevent docking to a full station
    // if (station.getBikes().size() >= station.getCapacity()) {
    // emitDockEvent(station, "FULL");
    // throw new IllegalStateException("Station is full");
    // }
    //
    // // R-BMS-06: Only allow return if bike is reserved (example logic)
    // Bike bike = bikeRepository.findById(bikeId)
    // .orElseThrow(() -> new RuntimeException("Bike not found"));
    // if (!bike.isReserved()) {
    // throw new IllegalStateException("Bike is not reserved for return");
    // }
    //
    // // Dock the bike
    // station.getBikes().add(bike);
    // station.setNumberOfBikesDocked(station.getBikes().size());
    //
    // // R-BMS-04: Record state transition with event ID
    // String eventId = UUID.randomUUID().toString();
    // recordStateTransition(station, bike, "DOCKED", eventId);
    //
    // // R-BMS-06/07: Emit events
    // emitBikeStatusEvent(bike, "RETURNED", eventId);
    // if (station.getBikes().size() == station.getCapacity()) {
    // emitDockEvent(station, "FULL");
    // }
    //
    // return bikeStationRepository.save(station);
    // }

}
