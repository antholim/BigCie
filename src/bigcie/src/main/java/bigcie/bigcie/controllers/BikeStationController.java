package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.BikeRequest.BikeStationRequest;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.interfaces.IAuthorizationService;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "Bike Stations", description = "Operations related to bike stations")
public class BikeStationController {
    private final IBikeStationService bikeStationService;
    private final IAuthorizationService authorizationService;


    public BikeStationController(IBikeStationService bikeStationService, IAuthorizationService authorizationService) {
        this.bikeStationService = bikeStationService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "Create a new bike station")
    @PostMapping
    public ResponseEntity<BikeStation> createStation(@RequestBody BikeStationRequest station, HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
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
    public ResponseEntity<BikeStation> updateStationStatus(@PathVariable UUID id, @RequestParam BikeStationStatus status) {
        BikeStation updatedStation = bikeStationService.updateStationStatus(id, status);
        return ResponseEntity.ok(updatedStation);
    }
}
