package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;

import java.util.List;
import java.util.UUID;

public interface IBikeStationService {
    BikeStation createStation(BikeStation station);
    BikeStation getStationById(UUID id);
    List<BikeStation> getAllStations();
    List<BikeStation> getStationsByStatus(BikeStationStatus status);
    BikeStation updateStation(UUID id, BikeStation station);
    void deleteStation(UUID id);
    BikeStation updateStationStatus(UUID id, BikeStationStatus status);
}

