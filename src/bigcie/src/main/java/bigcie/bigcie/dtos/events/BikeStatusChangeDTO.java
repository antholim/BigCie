package bigcie.bigcie.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BikeStatusChangeDTO {

    private String bikeId;
    private String newStatus;

}
