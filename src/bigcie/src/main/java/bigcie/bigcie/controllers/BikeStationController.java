package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.dtos.BikeStationRequest.MoveBikeRequest;
import bigcie.bigcie.dtos.DockingRequest.DockBikeRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.interfaces.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import bigcie.bigcie.exceptions.responses.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "Bike Stations", description = "Operations related to bike stations")
@Slf4j
public class BikeStationController {
    private final IBikeStationService bikeStationService;
    private final IAuthorizationService authorizationService;
    private final IReservationService reservationService;
    private final ITokenService tokenService;
    private final ICookieService cookieService;

    public BikeStationController(IBikeStationService bikeStationService, IAuthorizationService authorizationService,
            IReservationService reservationService, ITokenService tokenService, ICookieService cookieService) {
        this.bikeStationService = bikeStationService;
        this.authorizationService = authorizationService;
        this.reservationService = reservationService;
        this.tokenService = tokenService;
        this.cookieService = cookieService;
    }

    @Operation(summary = "Create a new bike station")
    @PostMapping
    public ResponseEntity<BikeStation> createStation(@RequestBody BikeStationRequest station,
            HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to create bike station");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BikeStation createdStation = bikeStationService.createStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }

    @Operation(summary = "Get a bike station by ID")
    @GetMapping("/{id}")
    public ResponseEntity<BikeStation> getStationById(@PathVariable UUID id) {
        BikeStation station = bikeStationService.getStationById(id);
        return ResponseEntity.ok(station);
    }

    @Operation(summary = "Get all bike stations")
    @GetMapping
    public ResponseEntity<List<BikeStation>> getAllStations() {
        List<BikeStation> stations = bikeStationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @Operation(summary = "Get bike stations by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BikeStation>> getStationsByStatus(@PathVariable BikeStationStatus status) {
        List<BikeStation> stations = bikeStationService.getStationsByStatus(status);
        return ResponseEntity.ok(stations);
    }

    @Operation(summary = "Make a reservation at a bike station")
    @PostMapping("/{stationId}/reserve")
    public ResponseEntity<Reservation> makeReservation(
            @PathVariable UUID stationId,
            HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        Reservation reservation = reservationService.createReservation(userId, stationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @Operation(summary = "Cancel a reservation")
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a reservation by ID")
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable UUID reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

    @Operation(summary = "Get all reservations")
    @GetMapping("/reservations")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Get all reservations for the authenticated user")
    @GetMapping("/reservations/me")
    public ResponseEntity<List<Reservation>> getAllActiveReservationsForUser(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        List<Reservation> reservations = reservationService.getAllActiveReservationsForUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "update a reservation")
    @PutMapping("/reservations/{reservationId}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable UUID reservationId,
            @RequestBody Reservation reservation) {
        Reservation updatedReservation = reservationService.updateReservation(reservationId, reservation);
        return ResponseEntity.ok(updatedReservation);
    }

    @Operation(summary = "Update a bike station")
    @PutMapping("/{id}")
    public ResponseEntity<BikeStation> updateStation(@PathVariable UUID id, @RequestBody BikeStation station) {
        BikeStation updatedStation = bikeStationService.updateStation(id, station);
        return ResponseEntity.ok(updatedStation);
    }

    @Operation(summary = "Delete a bike station")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable UUID id, HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bikeStationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update bike station status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<BikeStation> updateStationStatus(@PathVariable UUID id,
            @RequestParam BikeStationStatus status) {
        BikeStation updatedStation = bikeStationService.updateStationStatus(id, status);
        return ResponseEntity.ok(updatedStation);
    }

    @Operation(summary = "Dock a bike at a station")
    @PostMapping("/{stationId}/dock")
    public ResponseEntity<?> dockBike(
            @PathVariable UUID stationId,
            @RequestBody DockBikeRequest dockBikeRequest,
            HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        System.out.println(userId.toString());
        // Implement checks for docking here
        if (!bikeStationService.hasAvailableDocks(stationId)) {
            ErrorResponse err = new ErrorResponse("No available docks at this station");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        bikeStationService.dockBike(stationId, dockBikeRequest.getBikeId(), userId);
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Undock a bike from a station")
    @PostMapping("/{stationId}/undock")
    public ResponseEntity<BikeStation> undockBike(
            @RequestParam("bikeType") BikeType bikeType,
            @PathVariable UUID stationId,
            HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        System.out.println(bikeType.toString());
        bikeStationService.undockBike(stationId, userId, bikeType);
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Rebalance bikes between stations")
    @PostMapping("/rebalance-bikes")
    public ResponseEntity<BikeStation> rebalanceStation(HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to move bike station");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bikeStationService.rebalanceBikes();
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Move bike from source station to destination station")
    @PostMapping("/move-bike")
    public ResponseEntity<BikeStation> moveBike(
            HttpServletRequest request,
            @RequestBody MoveBikeRequest moveBikeRequest) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to create bike station");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bikeStationService.moveBike(moveBikeRequest);
        return ResponseEntity.ok(null);
    }
}
