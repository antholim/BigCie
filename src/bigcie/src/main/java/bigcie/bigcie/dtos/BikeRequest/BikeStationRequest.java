package bigcie.bigcie.dtos.BikeRequest;

import bigcie.bigcie.entities.enums.BikeStationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class BikeStationRequest {
    private String name;
    private BikeStationStatus status;
    private double latitude;
    private double longitude;
    private String address;
    private int capacity;
//    private int numberOfBikesDocked;
//    private Bike bikes[];
    private Duration reservationHoldTimeMinutes; // in minutes
}

