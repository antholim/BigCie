package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.BikeStationStatus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "bikes_station")
public class BikeStation {
    @MongoId
    private UUID id;
    private String name;
    private BikeStationStatus status;
    private double latitude;
    private double longitude;
    private String address;
    private int capacity;
    private int numberOfBikesDocked;
    private List<Bike> bikes;
    private int reservationHoldTimeMinutes; // in minutes
}
