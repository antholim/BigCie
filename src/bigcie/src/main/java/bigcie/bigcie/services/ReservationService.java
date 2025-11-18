package bigcie.bigcie.services;

import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.exceptions.StationIsEmptyException;
import bigcie.bigcie.exceptions.UserAlreadyHasReservationException;
import bigcie.bigcie.repositories.ReservationRepository;
import bigcie.bigcie.services.interfaces.IReservationService;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService implements IReservationService {
    private final ReservationRepository reservationRepository;
    private final BikeService bikeService;
    private final BikeStationService bikeStationService;

    public ReservationService(ReservationRepository reservationRepository,
            BikeService bikeService,
            BikeStationService bikeStationService) {
        this.reservationRepository = reservationRepository;
        this.bikeService = bikeService;
        this.bikeStationService = bikeStationService;
    }

    /**
     * Create a new reservation for a user at a specific bike station
     * 
     * @param userId        The ID of the user making the reservation
     * @param bikeStationId The ID of the bike station where the bike is reserved
     * @return The created reservation
     */
    public Reservation createReservation(UUID userId, UUID bikeStationId) {
        BikeStation station = bikeStationService.getStationById(bikeStationId);

        // check if client has reservation already
        if (reservationRepository.findByUserId(userId).stream()
                .anyMatch(reservation -> reservation.getBikeStationId().equals(bikeStationId))) {
            throw new UserAlreadyHasReservationException();
        }

        // Find an available bike at the station
        Bike availableBike = bikeStationService.getStationBikes(bikeStationId).stream()
                .filter(bike -> bike.getStatus() == BikeStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(StationIsEmptyException::new);

        // Create reservation
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                userId,
                bikeStationId,
                availableBike.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(station.getReservationHoldTimeMinutes()),
                ReservationStatus.ACTIVE
                );

        // Update bike status to RESERVED
        bikeService.updateBikeStatus(availableBike.getId(), BikeStatus.RESERVED);

        // Save and return reservation
        return reservationRepository.save(reservation);
    }

    /**
     * Get a reservation by its ID
     * 
     * @param reservationId The ID of the reservation
     * @return The reservation
     */
    public Reservation getReservationById(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
    }

    /**
     * Get all reservations for a specific user
     * 
     * @param userId The ID of the user
     * @return List of reservations for the user
     */
    public List<Reservation> getReservationsByUserId(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Get all reservations at a specific bike station
     * 
     * @param bikeStationId The ID of the bike station
     * @return List of reservations at the station
     */
    public List<Reservation> getReservationsByBikeStation(UUID bikeStationId) {
        return reservationRepository.findByBikeStationId(bikeStationId);
    }

    /**
     * Cancel a reservation
     * 
     * @param reservationId The ID of the reservation to cancel
     */
    public void cancelReservation(UUID reservationId) {
        Reservation reservation = getReservationById(reservationId);

        // Update bike status back to AVAILABLE
        Bike bike = bikeService.getBikeById(reservation.getBikeId());
        bikeService.updateBikeStatus(bike.getId(), BikeStatus.AVAILABLE);

        reservationRepository.deleteById(reservationId);
        }

    /**
     * Check if a reservation has expired
     * 
     * @param reservationId The ID of the reservation
     * @return true if the reservation has expired, false otherwise
     */
    public boolean isReservationExpired(UUID reservationId) {
        Reservation reservation = getReservationById(reservationId);
        return LocalDateTime.now().isAfter(reservation.getExpiry());
    }

    /**
     * Extend a reservation's expiry time
     * 
     * @param reservationId     The ID of the reservation
     * @param additionalMinutes The number of minutes to extend the reservation by
     * @return The updated reservation
     */
    public Reservation extendReservation(UUID reservationId, int additionalMinutes) {
        Reservation reservation = getReservationById(reservationId);

        if (isReservationExpired(reservationId)) {
            throw new RuntimeException("Cannot extend an expired reservation");
        }

        reservation.setExpiry(reservation.getExpiry().plusMinutes(additionalMinutes));
        return reservationRepository.save(reservation);
    }

    /**
     * Get all active reservations (not yet expired)
     * 
     * @return List of active reservations
     */
    public List<Reservation> getAllActiveReservations() {
        return reservationRepository.findAll().stream()
                .filter(reservation -> !isReservationExpired(reservation.getId()))
                .toList();
    }

    /**
     * Update a reservation
     * 
     * @param reservationId      The ID of the reservation
     * @param updatedReservation The updated reservation data
     * @return The updated reservation
     */
    public Reservation updateReservation(UUID reservationId, Reservation updatedReservation) {
        Reservation reservation = getReservationById(reservationId);

        if (updatedReservation.getExpiry() != null) {
            reservation.setExpiry(updatedReservation.getExpiry());
        }

        return reservationRepository.save(reservation);
    }

    /**
     * Delete a reservation by ID
     * 
     * @param reservationId The ID of the reservation
     */
    public void deleteReservation(UUID reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    /**
     * Get all reservations
     * 
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    @Override
    public List<Reservation> getAllActiveReservationsForUser(UUID userId) {
        return reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.ACTIVE);
    }

    @Override
    public List<Reservation> getExpiredReservationsPastYearByUserId(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.EXPIRED);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        return reservations.stream()
                .filter(reservation -> reservation.getExpiry().isAfter(oneYearAgo))
                .toList();
    }

    @Override
    public List<Reservation> getReservationsPastYearByUserIdAndStatus(UUID userId, ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(userId, status);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        return reservations.stream()
                .filter(reservation -> reservation.getExpiry().isAfter(oneYearAgo))
                .toList();
    }

    /**
     * Clean up expired reservations and release reserved bikes
     */
//    @Scheduled(fixedDelay = 60000) // Runs every minute
//    public void cleanupExpiredReservations() {
//        // Use MongoDB query to find expired reservations efficiently
//        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());
//
//        for (Reservation reservation : expiredReservations) {
//            Bike bike = bikeService.getBikeById(reservation.getBikeId());
//            bikeService.updateBikeStatus(bike.getId(), BikeStatus.AVAILABLE);
//            reservationRepository.deleteById(reservation.getId());
//        }
//    }
}
