package bigcie.bigcie.services;

import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.services.interfaces.IBMSConfigService;
import org.springframework.stereotype.Service;
import bigcie.bigcie.entities.BMSConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class BMSConfigService implements IBMSConfigService {
    private final BikeRepository bikeRepository;
    private final BikeStationRepository bikeStationRepository;
    private BMSConfig bmsConfig;

    public BMSConfigService(BikeRepository bikeRepository, BikeStationRepository bikeStationRepository) {
        this.bikeRepository = bikeRepository;
        this.bikeStationRepository = bikeStationRepository;
    }

    @Override
    public void populateDB() {
        try {
            /*
             * Docking stations
             * Docking station name
             * Status (empty | occupied | full | out_of_service)
             * Lat/long position
             * Street address
             * Capacity (# of bikes)
             * Number of bikes docked
             * Bikes: List of bikes docked
             * Reservation hold time (expiresAfterMinutes)
             * Bike
             * Id
             * Status (available | reserved | on_trip | maintenance)
             * Type: (standard | e-bike)
             */
            List<BikeStation> stations = bmsConfig.getBikeStations();
            List<Bike> bikes = bmsConfig.getBikes();
            // Save stations and bikes to the database
            bikeRepository.saveAll(bikes);
            bikeStationRepository.saveAll(stations);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public BMSConfig createConfig(String JSONFilePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            java.io.File file = new java.io.File(JSONFilePath);
            this.bmsConfig = objectMapper.readValue(file, BMSConfig.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the loaded config (or null if an exception occurred)
        return this.bmsConfig;
    }

    @Override
    public BMSConfig getBMSConfig() {
        // Implementation for returning the BMSConfig
        return this.bmsConfig;
    }

    // Uses entity bigcie.bigcie.entities.BMSConfig defined in entities package

}
