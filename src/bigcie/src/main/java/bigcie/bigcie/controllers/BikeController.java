package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.interfaces.IAuthorizationService;
import bigcie.bigcie.services.interfaces.IBikeService;
import bigcie.bigcie.services.interfaces.ICookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bikes")
@Tag(name = "Bikes", description = "Operations related to bikes")
public class BikeController {
    private final IBikeService bikeService;
    private final ICookieService cookieService;
    private final IAuthorizationService authorizationService;

    public BikeController(IBikeService bikeService, ICookieService cookieService, IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
        this.cookieService = cookieService;
        this.bikeService = bikeService;
    }

    @Operation(summary = "Create a new bike")
    @PostMapping
    public ResponseEntity<Bike> createBike(@RequestBody BikeRequest bike, HttpServletRequest request) {
        if (authorizationService.hasRole(request, UserType.OPERATOR)) {
            Bike createdBike = bikeService.createBike(bike);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBike);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Get a bike by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Bike> getBikeById(@PathVariable UUID id) {
        Bike bike = bikeService.getBikeById(id);
        return ResponseEntity.ok(bike);
    }

    @Operation(summary = "Get all bikes")
    @GetMapping
    public ResponseEntity<List<Bike>> getAllBikes() {
        List<Bike> bikes = bikeService.getAllBikes();
        return ResponseEntity.ok(bikes);
    }

    @Operation(summary = "Get bikes by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bike>> getBikesByStatus(@PathVariable BikeStatus status) {
        List<Bike> bikes = bikeService.getBikesByStatus(status);
        return ResponseEntity.ok(bikes);
    }

    @Operation(summary = "Update a bike")
    @PutMapping("/{id}")
    public ResponseEntity<Bike> updateBike(@PathVariable UUID id, @RequestBody Bike bike) {
        Bike updatedBike = bikeService.updateBike(id, bike);
        return ResponseEntity.ok(updatedBike);
    }

    @Operation(summary = "Delete a bike")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBike(@PathVariable UUID id, HttpServletRequest request) {
        if (authorizationService.hasRole(request, UserType.OPERATOR)) {
            bikeService.deleteBike(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Update bike status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Bike> updateBikeStatus(@PathVariable UUID id, @RequestParam BikeStatus status) {
        Bike updatedBike = bikeService.updateBikeStatus(id, status);
        return ResponseEntity.ok(updatedBike);
    }
}