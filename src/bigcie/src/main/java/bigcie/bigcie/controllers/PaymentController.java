package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest.PaymentPlanDto;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.services.PaymentService;
import bigcie.bigcie.services.TokenService;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Operations related to payment methods")
public class PaymentController {
    private final IPaymentService paymentService;
    private final TokenService tokenService;
    private final ICookieService cookieService;

    public PaymentController(PaymentService paymentService, TokenService tokenService, ICookieService cookieService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }
    @Operation(summary = "Get Payment Method", description = "Get the payment methods for the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<List<PaymentInfoDto>> getPaymentInfo(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        List<PaymentInfoDto> paymentInfo = paymentService.getPaymentInfo(userId);
        return ResponseEntity.ok(paymentInfo);
    }

    @Operation(summary = "Add Payment Method", description = "Add a new payment method for the authenticated user")
    @PostMapping("/add-payment")
    public ResponseEntity<?> addPaymentInfo(@RequestBody PaymentInfoRequest paymentInfoRequest,
            HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        paymentService.addPaymentMethod(userId, paymentInfoRequest);
        return ResponseEntity.ok("Payment method added successfully");
    }

    @Operation(summary = "Update Payment Method", description = "Set Default Payment Method for the authenticated user")
    @PatchMapping("/{id}/default")
    public ResponseEntity<?> addPaymentInfo(@PathVariable UUID id,
                                            HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        paymentService.updateDefaultPaymentMethod(userId, id);
        return ResponseEntity.ok("Default payment method updated successfully");
    }
    @Operation(summary = "Update Payment Plan", description = "Update the payment plan for the authenticated user")
    @PatchMapping("/update-plan")
    public ResponseEntity<?> updatePaymentPlan(@RequestBody PaymentPlanDto paymentPlanRequest,
                                               HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        paymentService.updatePaymentPlan(userId, paymentPlanRequest);
        return ResponseEntity.ok("Payment plan updated successfully");
    }
    @Operation(summary = "Get Current Plan", description = "Get the current payment plan for the authenticated user")
    @GetMapping("/current-plan")
    public ResponseEntity<PaymentPlanDto> getCurrentPaymentPlan(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        PaymentPlanDto pricingPlan = paymentService.getPricingPlanByUserId(userId);
        return ResponseEntity.ok(pricingPlan);
    }


}