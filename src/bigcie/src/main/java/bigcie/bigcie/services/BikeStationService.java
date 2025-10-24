package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import bigcie.bigcie.entities.enums.BikeStatus;

@Service
public class BikeStationService implements IBikeStationService {
    private final BikeStationRepository bikeStationRepository;
    private final EventService eventService;

    public BikeStationService(BikeStationRepository bikeStationRepository, EventService eventService) {
        this.bikeStationRepository = bikeStationRepository;
        this.eventService = eventService;
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
        
        BikeStation station = getStationById(stationId);
        
        // Record docking event
        eventService.recordStateTransition(
            "BikeStation",
            stationId,
            "BIKE_COUNT:" + station.getNumberOfBikesDocked(),
            "BIKE_COUNT:" + (station.getNumberOfBikesDocked() + 1)
        );
    }

    @Override
    public UUID undockBike(UUID stationId) {
        BikeStation station = getStationById(stationId);
        
        // Record undocking event
        eventService.recordStateTransition(
            "BikeStation",
            stationId,
            "BIKE_COUNT:" + station.getNumberOfBikesDocked(),
            "BIKE_COUNT:" + (station.getNumberOfBikesDocked() - 1)
        );
        
        return null;
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
}