package bigcie.bigcie.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bigcie.bigcie.dtos.LoginRequest;
import bigcie.bigcie.dtos.RegisterRequest;
import bigcie.bigcie.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return null;
//        return ResponseEntity.status(200).(authService.login(loginRequest)).;
    }

    @PostMapping("/api/v1/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return null;
//        return ResponseEntity.ok(authService.register(registerRequest));
    }
}
