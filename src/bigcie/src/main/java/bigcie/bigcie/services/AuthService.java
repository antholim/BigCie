package bigcie.bigcie.services;

import bigcie.bigcie.dtos.auth.LoginRequest;
import bigcie.bigcie.dtos.auth.RegisterRequest;
import bigcie.bigcie.dtos.auth.RegisterResponse;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.entities.factory.UserFactory;
import bigcie.bigcie.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(), loginRequest.getPassword()
            )
        );
        if (authentication.isAuthenticated()) {
            // Retrieve user entity by username or email
            User user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(loginRequest.getUsernameOrEmail()).orElse(null));
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            return tokenService.generateToken(user.getId(), (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal(), TokenType.ACCESS_TOKEN);
        }
        throw new IllegalArgumentException("Invalid username/email or password");
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.getUsername());
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        User user;
        UserFactory userFactory = UserFactory.getInstance();
        user = userFactory.createUser(registerRequest.getUserType());
        if (user == null) {
            throw new IllegalArgumentException("Invalid user type: " + registerRequest.getUserType());
        }
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setId(UUID.randomUUID());
        userRepository.save(user);

        return RegisterResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getType())
                .message("Registration successful for: " + user.getUsername())
                .timestamp(Instant.now())
                .build();
    }
}
