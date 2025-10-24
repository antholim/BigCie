package bigcie.bigcie.dtos.ReservationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationRequest {
    private String bikeId;
    private String userId;
    private String stationId;
    private int durationMinutes;

}
