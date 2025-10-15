package bigcie.bigcie.dtos.auth;

import bigcie.bigcie.entities.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserType userType = UserType.RIDER;
    private String address;
}
