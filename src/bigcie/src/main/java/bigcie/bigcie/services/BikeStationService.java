package bigcie.bigcie.services;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import lombok.RequiredArgsConstructor;
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
    public BikeStation createStation(BikeStation station) {
        if (station.getId() == null) {
            station.setId(UUID.randomUUID());
        }
        return bikeStationRepository.save(station);
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
}
