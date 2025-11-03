package bigcie.bigcie.services.read;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.read.interfaces.IBikeStationLookup;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class BikeStationLookup implements IBikeStationLookup {
    private final BikeStationRepository bikeStationRepository;

    public BikeStationLookup(BikeStationRepository bikeStationRepository) {
        this.bikeStationRepository = bikeStationRepository;
    }

    @Override
    public String getStationNameById(UUID bikeStationId) {
        BikeStation station = bikeStationRepository.findById(bikeStationId)
                .orElseThrow(() -> new NoSuchElementException("station not found"));
        if (station != null) {
            return station.getName();
        }
        return "";
    }
}
