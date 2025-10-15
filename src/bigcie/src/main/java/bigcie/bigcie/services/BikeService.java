package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.services.interfaces.IBikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BikeService implements IBikeService {
    private final BikeRepository bikeRepository;
    private final BikeStationService bikeStationService;

    public BikeService(BikeRepository bikeRepository, BikeStationService bikeStationService) {
        this.bikeStationService = bikeStationService;
        this.bikeRepository = bikeRepository;
    }

    @Override
    public Bike createBike(BikeRequest bike) {
        BikeStation station = bikeStationService.getStationById(bike.getBikeStationId());
        Bike bikeEntity = new Bike();
        bikeEntity.setId(UUID.randomUUID());
        bikeEntity.setBikeType(bike.getBikeType());
        bikeEntity.setStatus(bike.getStatus());
        bikeEntity.setReservationExpiry(bike.getReservationExpiry());

        Bike savedBike = bikeRepository.save(bikeEntity);
        station.getBikes().add(savedBike);
        bikeStationService.updateStation(station.getId(), station);
        return savedBike;
    }

    @Override
    public Bike getBikeById(UUID id) {
        return bikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bike not found with id: " + id));
    }

    @Override
    public List<Bike> getAllBikes() {
        return bikeRepository.findAll();
    }

    @Override
    public List<Bike> getBikesByStatus(BikeStatus status) {
        return bikeRepository.findByStatus(status);
    }

    @Override
    public Bike updateBike(UUID id, Bike bike) {
        Bike existingBike = getBikeById(id);
        existingBike.setStatus(bike.getStatus());
        existingBike.setBikeType(bike.getBikeType());
        existingBike.setReservationExpiry(bike.getReservationExpiry());
        return bikeRepository.save(existingBike);
    }

    @Override
    public void deleteBike(UUID id) {
        bikeRepository.deleteById(id);
    }

    @Override
    public Bike updateBikeStatus(UUID id, BikeStatus status) {
        Bike bike = getBikeById(id);
        bike.setStatus(status);
        return bikeRepository.save(bike);
    }
}
