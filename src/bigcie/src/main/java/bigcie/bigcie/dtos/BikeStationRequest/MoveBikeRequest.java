package bigcie.bigcie.dtos.BikeStationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class MoveBikeRequest {
    UUID sourceStationId;
    UUID destinationStationId;
}
