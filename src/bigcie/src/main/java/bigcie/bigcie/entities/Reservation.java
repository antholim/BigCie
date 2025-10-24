package bigcie.bigcie.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Reservation {
    private UUID id;
    private UUID userId;
    private UUID bikeStationId;
    private UUID bikeId;
    private LocalDateTime startTime;
    private LocalDateTime expiry;

}
