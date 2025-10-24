package bigcie.bigcie.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DockStatusDTO {

    private String dockId;
    private String status;

}
