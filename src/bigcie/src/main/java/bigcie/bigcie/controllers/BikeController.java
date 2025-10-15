package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.interfaces.IAuthorizationService;
import bigcie.bigcie.services.interfaces.IBikeService;
import bigcie.bigcie.services.interfaces.ICookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bikes")
public class BikeController {
    private final IBikeService bikeService;
    private final ICookieService cookieService;
    private final IAuthorizationService authorizationService;

    public BikeController(IBikeService bikeService, ICookieService cookieService, IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
        this.cookieService = cookieService;
        this.bikeService = bikeService;
    }

    @PostMapping
    public ResponseEntity<Bike> createBike(@RequestBody BikeRequest bike, HttpServletRequest request) {
        if (authorizationService.hasRole(request, UserType.OPERATOR)) {
            Bike createdBike = bikeService.createBike(bike);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBike);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bike> getBikeById(@PathVariable UUID id) {
        Bike bike = bikeService.getBikeById(id);
        return ResponseEntity.ok(bike);
    }

    @GetMapping
    public ResponseEntity<List<Bike>> getAllBikes() {
        List<Bike> bikes = bikeService.getAllBikes();
        return ResponseEntity.ok(bikes);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bike>> getBikesByStatus(@PathVariable BikeStatus status) {
        List<Bike> bikes = bikeService.getBikesByStatus(status);
        return ResponseEntity.ok(bikes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bike> updateBike(@PathVariable UUID id, @RequestBody Bike bike) {
        Bike updatedBike = bikeService.updateBike(id, bike);
        return ResponseEntity.ok(updatedBike);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBike(@PathVariable UUID id, HttpServletRequest request) {
        if (authorizationService.hasRole(request, UserType.OPERATOR)) {
            bikeService.deleteBike(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Bike> updateBikeStatus(@PathVariable UUID id, @RequestParam BikeStatus status) {
        Bike updatedBike = bikeService.updateBikeStatus(id, status);
        return ResponseEntity.ok(updatedBike);
    }
}