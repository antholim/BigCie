package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BikeStationService implements IBikeStationService {
    private final BikeStationRepository bikeStationRepository;

    public BikeStationService(BikeStationRepository bikeStationRepository) {
        this.bikeStationRepository = bikeStationRepository;
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

    }

    @Override
    public UUID undockBike(UUID stationId) {
        return null;
    }
//    @Override
//    public BikeStation dockBike(UUID stationId, UUID bikeId) {
//        BikeStation station = getStationById(stationId);
//
//        // R-BMS-05: Block if out of service
//        if (station.getStatus() == BikeStationStatus.OUT_OF_SERVICE) {
//            throw new IllegalStateException("Station is out of service");
//        }
//
//        // R-BMS-02: Prevent docking to a full station
//        if (station.getBikes().size() >= station.getCapacity()) {
//            emitDockEvent(station, "FULL");
//            throw new IllegalStateException("Station is full");
//        }
//
//        // R-BMS-06: Only allow return if bike is reserved (example logic)
//        Bike bike = bikeRepository.findById(bikeId)
//                .orElseThrow(() -> new RuntimeException("Bike not found"));
//        if (!bike.isReserved()) {
//            throw new IllegalStateException("Bike is not reserved for return");
//        }
//
//        // Dock the bike
//        station.getBikes().add(bike);
//        station.setNumberOfBikesDocked(station.getBikes().size());
//
//        // R-BMS-04: Record state transition with event ID
//        String eventId = UUID.randomUUID().toString();
//        recordStateTransition(station, bike, "DOCKED", eventId);
//
//        // R-BMS-06/07: Emit events
//        emitBikeStatusEvent(bike, "RETURNED", eventId);
//        if (station.getBikes().size() == station.getCapacity()) {
//            emitDockEvent(station, "FULL");
//        }
//
//        return bikeStationRepository.save(station);
//    }

}
