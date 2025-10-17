package bigcie.bigcie.entities;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "bms_config")
public class BMSConfig {
    private List<BikeStation> bikeStations;
    private List<Bike> bikes;
}
