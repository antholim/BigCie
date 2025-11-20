package bigcie.bigcie.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bigcie.bigcie.dtos.auth.LoginRequest;
import bigcie.bigcie.dtos.auth.RegisterRequest;
import bigcie.bigcie.services.AuthService;
import bigcie.bigcie.utils.IpAddressUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Authenticate user and return login response")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @Operation(summary = "Register a new user (rider, operator, or dual-role)")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        String clientIp = IpAddressUtil.getClientIp(request);
        return ResponseEntity.ok(authService.register(registerRequest, clientIp));
    }

    @Operation(summary = "Get your client IP address")
    @GetMapping("/my-ip")
    public ResponseEntity<?> getMyIp(HttpServletRequest request) {
        String clientIp = IpAddressUtil.getClientIp(request);
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("ip", clientIp);
        }});
    }
}