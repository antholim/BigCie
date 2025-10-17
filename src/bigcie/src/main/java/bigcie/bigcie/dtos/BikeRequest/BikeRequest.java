package bigcie.bigcie.dtos.BikeRequest;

import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.BikeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BikeRequest {
    private UUID bikeStationId;
    private BikeStatus status;
    private BikeType bikeType;

    @Schema(required = false, description = "Reservation expiry date/time (optional)")
    private LocalDateTime reservationExpiry;
}
