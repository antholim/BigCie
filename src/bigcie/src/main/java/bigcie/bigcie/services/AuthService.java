package bigcie.bigcie.services;

import bigcie.bigcie.dtos.auth.LoginRequest;
import bigcie.bigcie.dtos.auth.LoginResponse;
import bigcie.bigcie.dtos.auth.RegisterRequest;
import bigcie.bigcie.dtos.auth.RegisterResponse;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.entities.factory.UserFactory;
import bigcie.bigcie.models.AuthenticationResponse;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.ITokenService;
import bigcie.bigcie.config.OperatorRegistrationConfig;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ITokenService tokenService;
    private final ICookieService cookieService;
    private final OperatorRegistrationConfig operatorRegistrationConfig;

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                    .orElseGet(() -> userRepository.findByEmail(loginRequest.getUsernameOrEmail()).orElse(null));
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);
            String refreshToken = tokenService.generateToken(user, TokenType.REFRESH_TOKEN);
            AuthenticationResponse res = new AuthenticationResponse(accessToken, refreshToken);
            cookieService.addTokenCookies(response, res);
            return LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userType(user.getType())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .message("Login successful for: " + user.getUsername())
                    .timestamp(Instant.now())
                    .build();
        }
        throw new IllegalArgumentException("Invalid username/email or password");
    }

    public RegisterResponse register(RegisterRequest registerRequest, String clientIp) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.getUsername());
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        // Check IP restrictions for operator registration
        if (registerRequest.getUserType().equals(bigcie.bigcie.entities.enums.UserType.OPERATOR) ||
                registerRequest.getUserType().equals(bigcie.bigcie.entities.enums.UserType.DUAL_ROLE)) {
            if (!operatorRegistrationConfig.isIpAllowed(clientIp)) {
                throw new IllegalArgumentException(
                        "Your IP address (" + clientIp + ") is not authorized to register operators");
            }
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
