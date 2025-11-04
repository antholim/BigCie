package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.services.AuthorizationService;
import bigcie.bigcie.services.TokenService;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.ITripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@Tag(name = "Payments", description = "Operations related to payment methods")
public class TripController {

    private final AuthorizationService authorizationService;
    private final TokenService tokenService;
    private final ICookieService cookieService;
    private final ITripService tripService;

    public TripController(TokenService tokenService, ICookieService cookieService, ITripService tripService,
            AuthorizationService authorizationService) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.tripService = tripService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "Get Trip History", description = "Get the trip history for the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<List<TripDto>> getTripInfo(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        List<TripDto> tripDtoList = tripService.getTripByUserId(userId);
        return ResponseEntity.ok(tripDtoList);
    }

    @Operation(summary = "Get All Trips", description = "Get all trips for admin purposes")
    @GetMapping("/getAll")
    // we need to use the isOperator method to check if the user is an admin
    public ResponseEntity<List<TripDto>> getAllTrips(HttpServletRequest request) {
        if (authorizationService.isOperator(request)) {
            List<TripDto> tripDtoList = tripService.getAllTrips();
            return ResponseEntity.ok(tripDtoList);
        } else {
            return ResponseEntity.status(403).build(); // Forbidden
        }
    }

}
