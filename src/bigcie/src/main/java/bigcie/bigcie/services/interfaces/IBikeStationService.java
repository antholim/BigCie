package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.Reservation;

import java.util.List;
import java.util.UUID;

public interface IBikeStationService {
    BikeStation createStation(BikeStationRequest station);

    BikeStation getStationById(UUID id);

    List<BikeStation> getAllStations();

    List<BikeStation> getStationsByStatus(BikeStationStatus status);

    BikeStation updateStation(UUID id, BikeStation station);

    void deleteStation(UUID id);

    BikeStation updateStationStatus(UUID id, BikeStationStatus status);

    void dockBike(UUID stationId, UUID bikeId, UUID userId);

    UUID undockBike(UUID stationId, UUID userId);

    boolean hasAvailableDocks(UUID stationId);

    boolean hasAvailableBikes(UUID stationId);

    boolean isEmpty(UUID stationId);

    void holdBike(UUID stationId);
}
