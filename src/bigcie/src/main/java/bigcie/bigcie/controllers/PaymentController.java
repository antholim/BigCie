package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.Billing.BillDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest.PaymentPlanDto;
import bigcie.bigcie.services.PaymentService;
import bigcie.bigcie.services.TokenService;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.IPaymentService;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bigcie.bigcie.services.interfaces.IPriceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Operations related to payment methods")
public class PaymentController {
    private final IPaymentService paymentService;
    private final TokenService tokenService;
    private final ICookieService cookieService;
    private final IPriceService priceService;
    private final IFlexDollarService flexDollarService;

    public PaymentController(PaymentService paymentService, TokenService tokenService, ICookieService cookieService,
            IPriceService priceService, IFlexDollarService flexDollarService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.priceService = priceService;
        this.flexDollarService = flexDollarService;
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

    @Operation(summary = "Get Billing Info", description = "Get the billing information for the authenticated user")
    @GetMapping("/billing-info")
    public ResponseEntity<List<BillDto>> getBillingInfo(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        List<BillDto> billingInfo = paymentService.getBillingInfo(userId);
        return ResponseEntity.ok(billingInfo);
    }

    // send the 5min rate
    @Operation(summary = "Get 5-Minute Rate", description = "Get the current 5-minute rate for bike rentals")
    @GetMapping("/5min-rate")
    public ResponseEntity<Double> getFiveMinuteRate() {
        double rate = priceService.getFiveMinuteRate();
        return ResponseEntity.ok(rate);
    }

    // send the e-bike surcharge
    @Operation(summary = "Get E-Bike Surcharge", description = "Get the current e-bike surcharge for bike rentals")
    @GetMapping("/ebike-surcharge")
    public ResponseEntity<Double> getEBikeSurcharge() {
        double surcharge = priceService.getEBikeSurcharge();
        return ResponseEntity.ok(surcharge);
    }

    @Operation(summary = "Get Flex Dollar Balance", description = "Get the current flex dollar balance for the authenticated user")
    @GetMapping("/flex-dollars")
    public ResponseEntity<Double> getFlexDollarBalance(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        double balance = flexDollarService.getFlexDollarBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Reset Flex Dollar Balance", description = "Reset the flex dollar balance to 0 for the authenticated user (dev/testing only)")
    @DeleteMapping("/flex-dollars/reset")
    public ResponseEntity<?> resetFlexDollarBalance(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        flexDollarService.resetFlexDollars(userId);
        return ResponseEntity.ok("Flex dollar balance reset to 0");
    }

}