package bigcie.bigcie.services;

import bigcie.bigcie.dtos.LoginRequest;
import bigcie.bigcie.dtos.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String login(LoginRequest loginRequest) {
        // Stub: Replace with real authentication logic
        return "Login successful for: " + loginRequest.getUsernameOrEmail();
    }

    public String register(RegisterRequest registerRequest) {
        // Stub: Replace with real registration logic
        return "Registration successful for: " + registerRequest.getUsername();
    }
}
