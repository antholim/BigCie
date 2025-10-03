package bigcie.bigcie.services;

import bigcie.bigcie.dtos.LoginRequest;
import bigcie.bigcie.dtos.RegisterRequest;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.models.UserFactory;
import bigcie.bigcie.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
            return tokenService.generateToken(user.getId(), (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal(), bigcie.bigcie.models.enums.TokenType.ACCESS_TOKEN);
        }
        throw new IllegalArgumentException("Invalid username/email or password");
    }

    public String register(RegisterRequest registerRequest) {
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
        userRepository.save(user);
        return "Registration successful for: " + registerRequest.getUsername();
    }
}
