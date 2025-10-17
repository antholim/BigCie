package bigcie.bigcie.exceptions.responses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponse {

    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

}
