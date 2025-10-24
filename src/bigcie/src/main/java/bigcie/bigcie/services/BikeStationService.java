package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
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
<<<<<<< HEAD
    private final BikeStationRepository bikeStationRepository;
    private final EventService eventService;

    public BikeStationService(BikeStationRepository bikeStationRepository, EventService eventService) {
        this.bikeStationRepository = bikeStationRepository;
        this.eventService = eventService;
=======

    private final BikeRepository bikeRepository;
    private final BikeStationRepository bikeStationRepository;
    private final ReservationRepository reservationRepository;
    private final BikeService bikeService;

    public BikeStationService(BikeStationRepository bikeStationRepository, ReservationRepository reservationRepository,
            BikeRepository bikeRepository, BikeService bikeService) {
        this.bikeStationRepository = bikeStationRepository;
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.bikeService = bikeService;
>>>>>>> 96595e3d1e9f22ac5689bd559ffc67499fcf90d7
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
        
        BikeStation savedStation = bikeStationRepository.save(bikeStationEntity);
        
        // Record creation event
        eventService.recordStateTransition(
            "BikeStation",
            savedStation.getId(),
            null,
            station.getStatus().toString()
        );
        
        return savedStation;
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
        
        // Record status change if different
        if (!existingStation.getStatus().equals(station.getStatus())) {
            eventService.recordStateTransition(
                "BikeStation",
                id,
                existingStation.getStatus().toString(),
                station.getStatus().toString()
            );
        }
        
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
        BikeStation station = getStationById(id);
        
        // Record deletion event
        eventService.recordStateTransition(
            "BikeStation",
            id,
            station.getStatus().toString(),
            "DELETED"
        );
        
        bikeStationRepository.deleteById(id);
    }

    @Override
    public BikeStation updateStationStatus(UUID id, BikeStationStatus status) {
        BikeStation station = getStationById(id);
        String oldStatus = station.getStatus().toString();
        
        station.setStatus(status);
        BikeStation updatedStation = bikeStationRepository.save(station);
        
        // R-BMS-04: Record state transition
        eventService.recordStateTransition(
            "BikeStation",
            id,
            oldStatus,
            status.toString()
        );
        
        return updatedStation;
    }

    @Override
    public void dockBike(UUID stationId, UUID bikeId) {
        // To dock a bike, the bike needs to be of status "ON_TRIP"
        // Check if station is not full
        // Check if dock is not out of service
<<<<<<< HEAD
        
        BikeStation station = getStationById(stationId);
        
        // Record docking event
        eventService.recordStateTransition(
            "BikeStation",
            stationId,
            "BIKE_COUNT:" + station.getNumberOfBikesDocked(),
            "BIKE_COUNT:" + (station.getNumberOfBikesDocked() + 1)
        );
=======
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
        bike.setStatus(BikeStatus.AVAILABLE);
        bikeService.updateBike(bike.getId(), bike);
        bikeStationRepository.save(station);

>>>>>>> 96595e3d1e9f22ac5689bd559ffc67499fcf90d7
    }

    @Override
    public UUID undockBike(UUID stationId) {
<<<<<<< HEAD
        BikeStation station = getStationById(stationId);
        
        // Record undocking event
        eventService.recordStateTransition(
            "BikeStation",
            stationId,
            "BIKE_COUNT:" + station.getNumberOfBikesDocked(),
            "BIKE_COUNT:" + (station.getNumberOfBikesDocked() - 1)
        );
        
        return null;
=======
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
        bike.setStatus(BikeStatus.AVAILABLE);

        // make reservation null if any
        station.getBikes().remove(bike);
        station.setNumberOfBikesDocked(station.getNumberOfBikesDocked() - 1);
        bike.setStatus(BikeStatus.ON_TRIP);
        /*
         * Reservation reservation = reservationRepository.findByBikeId(bike.getId());
         * if (reservation != null) {
         * reservation.setActive(false);
         * }
         */

        return bike.getId();

>>>>>>> 96595e3d1e9f22ac5689bd559ffc67499fcf90d7
    }

    @Override
    public boolean hasAvailableDocks(UUID stationId) {
        BikeStation station = getStationById(stationId);
        return station.getNumberOfBikesDocked() < station.getCapacity();
    }

    @Override
    public boolean hasAvailableBikes(UUID stationId) {
        BikeStation station = getStationById(stationId);
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
<<<<<<< HEAD
}
=======

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
        bike.setStatus(BikeStatus.RESERVED);
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
>>>>>>> 96595e3d1e9f22ac5689bd559ffc67499fcf90d7
