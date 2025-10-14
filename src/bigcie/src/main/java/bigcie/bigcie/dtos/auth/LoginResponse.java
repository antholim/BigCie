package bigcie.bigcie.dtos.auth;

import java.time.Instant;
import java.util.UUID;

import bigcie.bigcie.entities.enums.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private UUID userId;
    private String username;
    private String email;
    private UserType userType;
    private String accessToken;
    private String refreshToken;
    private String message;
    private Instant timestamp;
}
