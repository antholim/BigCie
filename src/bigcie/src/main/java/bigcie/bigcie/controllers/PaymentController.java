package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.auth.LoginRequest;
import bigcie.bigcie.dtos.auth.RegisterRequest;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.services.AuthorizationService;
import bigcie.bigcie.services.PaymentService;
import bigcie.bigcie.services.TokenService;
import bigcie.bigcie.services.interfaces.ICookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Payments", description = "Operations related to payment methods")
public class PaymentController {
    private final PaymentService paymentService;
    private final TokenService tokenService;
    private final ICookieService cookieService;

    public PaymentController(PaymentService paymentService, TokenService tokenService, ICookieService cookieService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }

    @Operation(summary = "Add Payment Method", description = "Add a new payment method for the authenticated user")
    @PostMapping("/add-payment")
    public ResponseEntity<?> addPaymentInfo(@RequestBody PaymentInfo paymentInfo, HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        paymentService.addPaymentMethod(userId,paymentInfo);
        return ResponseEntity.ok("Payment method added successfully");
    }

}