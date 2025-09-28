package bigcie.bigcie.services;

import bigcie.bigcie.dtos.LoginRequest;
import bigcie.bigcie.dtos.RegisterRequest;
import bigcie.bigcie.models.Operator;
import bigcie.bigcie.models.Rider;
import bigcie.bigcie.models.User;
import bigcie.bigcie.models.UserType;
import bigcie.bigcie.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public String login(LoginRequest loginRequest) {
        return "Login successful for: " + loginRequest.getUsernameOrEmail();
    }

    public String register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.getUsername());
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        User user;
        switch (registerRequest.getUserType()) {
            case OPERATOR -> user = new Operator();
            case RIDER -> user = new Rider();
            default -> throw new IllegalArgumentException("Invalid user type: " + registerRequest.getUserType());
        }
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        return "Registration successful for: " + registerRequest.getUsername();
    }
}
