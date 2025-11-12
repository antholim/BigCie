package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.services.AuthorizationService;
import bigcie.bigcie.services.interfaces.ITripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/trips")
@Tag(name = "Payments", description = "Operations related to payment methods")
public class TripController {

    private final AuthorizationService authorizationService;
    private final ITripService tripService;

    public TripController(ITripService tripService, AuthorizationService authorizationService) {
        this.tripService = tripService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "Get All Trips", description = "Get all trips for admin purposes")
    @GetMapping("/getAll")
    // we need to use the isOperator method to check if the user is an admin
    public ResponseEntity<List<TripDto>> getAllTrips(HttpServletRequest request) {
        log.info("getAllTrips endpoint called");
        log.info("Authorization header: {}", request.getHeader("Authorization"));
        try {
            boolean isOperator = authorizationService.isOperator(request);
            log.info("isOperator: {}", isOperator);
            if (isOperator) {
                List<TripDto> tripDtoList = tripService.getAllTrips();
                log.info("Returning {} trips", tripDtoList.size());
                return ResponseEntity.ok(tripDtoList);
            } else {
                log.warn("User is not an operator, returning 403");
                return ResponseEntity.status(403).build(); // Forbidden
            }
        } catch (Exception e) {
            log.error("Error in getAllTrips: ", e);
            throw e;
        }
    }

    @Operation(summary = "Get Current User Trips", description = "Get all trips for the current authenticated user")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserTrips(
            HttpServletRequest request,
            Pageable pageable,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        log.info("getCurrentUserTrips endpoint called");

        try {
            User user = authorizationService.getUserFromRequest(request);
            log.info("Getting trips for user: {}", user.getId());

            if (page != null || size != null) {
                Page<TripDto> tripPage = tripService.getTripByUserId(user.getId(), pageable);
                log.info("Returning paginated trips for user {} — page: {}, size: {}, total elements: {}",
                        user.getId(),
                        tripPage.getNumber(),
                        tripPage.getSize(),
                        tripPage.getTotalElements());
                return ResponseEntity.ok(tripPage);
            }

            List<TripDto> tripList = tripService.getTripByUserId(user.getId());
            log.info("Returning {} trips for user {}", tripList.size(), user.getId());
            return ResponseEntity.ok(tripList);

        } catch (Exception e) {
            log.error("Error in getCurrentUserTrips for request {}: ", request.getRequestURI(), e);
            throw e;
        }
    }


}
