package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.enums.BikeStatus;

import java.util.List;
import java.util.UUID;

public interface IBikeService {
    Bike createBike(Bike bike);
    Bike getBikeById(UUID id);
    List<Bike> getAllBikes();
    List<Bike> getBikesByStatus(BikeStatus status);
    Bike updateBike(UUID id, Bike bike);
    void deleteBike(UUID id);
    Bike updateBikeStatus(UUID id, BikeStatus status);
}
