package bigcie.bigcie.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationChangeDTO {

    private String reservationId;
    private String newStatus;
}
