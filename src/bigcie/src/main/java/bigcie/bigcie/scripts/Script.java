package bigcie.bigcie.scripts;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.services.interfaces.IBikeService;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class Script implements CommandLineRunner {
    private final IUserService userService;
    private final IBikeStationService bikeStationService;
    private final IBikeService bikeService;

    public Script(IUserService userService, IBikeStationService bikeStationService, IBikeService bikeService) {
        this.userService = userService;
        this.bikeStationService = bikeStationService;
        this.bikeService = bikeService;
    }

    @Override
    public void run(String... args) throws Exception {
        // //
        // clearBikeFromUser(UUID.fromString("93e21f50-5bf8-4891-9d3a-30a9676f3b36"));
        // setStationStatus();
        recountAllStations();
    }

    private void assignBikeToStation() {

    }

    private void clearBikeFromUser(UUID userId) {
        Rider rider = (Rider) userService.getUserByUUID(userId);
        rider.setCurrentBikes(new ArrayList<>());
        userService.updateUser(rider);
    }

    private void recountAllStations() {
        bikeStationService.getAllStations().forEach(station -> {
            int standardBikes = 0;
            int eBikes = 0;

            for (UUID bikeId : station.getBikesIds()) {
                if (bikeService.getBikeById(bikeId).getBikeType() == BikeType.STANDARD) {
                    standardBikes++;
                } else {
                    eBikes++;
                }
            }

            station.setStandardBikesDocked(standardBikes);
            station.setEBikesDocked(eBikes);
            bikeStationService.updateStation(station.getId(), station);
        });
    }

    private void setStationStatus() {
        bikeStationService.getAllStations().forEach(station -> {
            int bikes = station.getBikesIds().size();
            BikeStationStatus status = (bikes == 0) ? BikeStationStatus.EMPTY
                    : (bikes == station.getCapacity()) ? BikeStationStatus.FULL
                            : BikeStationStatus.OCCUPIED;

            station.setStatus(status);
            bikeStationService.updateStation(station.getId(), station);
        });
    }

}
