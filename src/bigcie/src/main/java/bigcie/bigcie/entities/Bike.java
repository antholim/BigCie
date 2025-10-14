package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.BikeType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "bikes")
public class Bike {
    @MongoId
    private UUID id;
    private BikeStatus status;
    private BikeType bikeType;
    private LocalDateTime reservationExpiry;
}
