package bigcie.bigcie.services;

import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.exceptions.StationIsEmptyException;
import bigcie.bigcie.exceptions.UserAlreadyHasReservationException;
import bigcie.bigcie.exceptions.UserIsNotRiderException;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import bigcie.bigcie.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ReservationService Unit Tests")
@ActiveProfiles("test")
class ReservationServiceTest {

    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BikeService bikeService;

    @Mock
    private BikeStationService bikeStationService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationService(reservationRepository, bikeService, bikeStationService, userService);
    }

    // ======================== createReservation Tests ========================

    @Test
    @DisplayName("Should create a reservation successfully for a rider")
    void testCreateReservationSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();
        UUID bikeId = UUID.randomUUID();

        Rider rider = mock(Rider.class);
        when(rider.getId()).thenReturn(userId);
        when(rider.getLoyaltyTier()).thenReturn(LoyaltyTier.BRONZE);

        BikeStation station = mock(BikeStation.class);
        when(station.getReservationHoldTimeMinutes()).thenReturn(15);

        Bike availableBike = mock(Bike.class);
        when(availableBike.getId()).thenReturn(bikeId);
        when(availableBike.getStatus()).thenReturn(BikeStatus.AVAILABLE);

        when(userService.getUserByUUID(userId)).thenReturn(rider);
        when(bikeStationService.getStationById(bikeStationId)).thenReturn(station);
        when(bikeStationService.getStationBikes(bikeStationId)).thenReturn(Collections.singletonList(availableBike));
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Reservation result = reservationService.createReservation(userId, bikeStationId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(bikeStationId, result.getBikeStationId());
        assertEquals(bikeId, result.getBikeId());
        assertEquals(ReservationStatus.ACTIVE, result.getStatus());
        verify(bikeService).updateBikeStatus(bikeId, BikeStatus.RESERVED);
    }

    @Test
    @DisplayName("Should throw UserIsNotRiderException when user is not a Rider")
    void testCreateReservationForNonRider() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();

        User nonRider = mock(User.class);
        when(nonRider.getId()).thenReturn(userId);

        when(userService.getUserByUUID(userId)).thenReturn(nonRider);

        // Act & Assert
        assertThrows(UserIsNotRiderException.class, () -> 
            reservationService.createReservation(userId, bikeStationId));
    }

    @Test
    @DisplayName("Should throw StationIsEmptyException when no bikes available")
    void testCreateReservationWhenStationEmpty() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();

        Rider rider = mock(Rider.class);
        when(rider.getId()).thenReturn(userId);
        when(rider.getLoyaltyTier()).thenReturn(LoyaltyTier.SILVER);

        BikeStation station = mock(BikeStation.class);
        when(station.getReservationHoldTimeMinutes()).thenReturn(15);

        when(userService.getUserByUUID(userId)).thenReturn(rider);
        when(bikeStationService.getStationById(bikeStationId)).thenReturn(station);
        when(bikeStationService.getStationBikes(bikeStationId)).thenReturn(Collections.emptyList());
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(StationIsEmptyException.class, () -> 
            reservationService.createReservation(userId, bikeStationId));
    }

    @Test
    @DisplayName("Should throw UserAlreadyHasReservationException for duplicate reservation")
    void testCreateReservationDuplicate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();

        Rider rider = mock(Rider.class);
        when(rider.getId()).thenReturn(userId);
        when(rider.getLoyaltyTier()).thenReturn(LoyaltyTier.GOLD);

        Reservation existingReservation = mock(Reservation.class);
        when(existingReservation.getStatus()).thenReturn(ReservationStatus.ACTIVE);
        when(existingReservation.getBikeStationId()).thenReturn(bikeStationId);

        when(userService.getUserByUUID(userId)).thenReturn(rider);
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.singletonList(existingReservation));

        // Act & Assert
        assertThrows(UserAlreadyHasReservationException.class, () -> 
            reservationService.createReservation(userId, bikeStationId));
    }

    @Test
    @DisplayName("Should apply loyalty tier bonus to reservation expiry time")
    void testCreateReservationAppliesLoyaltyBonus() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();
        UUID bikeId = UUID.randomUUID();

        Rider rider = mock(Rider.class);
        when(rider.getId()).thenReturn(userId);
        when(rider.getLoyaltyTier()).thenReturn(LoyaltyTier.GOLD); // Highest tier

        BikeStation station = mock(BikeStation.class);
        when(station.getReservationHoldTimeMinutes()).thenReturn(15);

        Bike availableBike = mock(Bike.class);
        when(availableBike.getId()).thenReturn(bikeId);
        when(availableBike.getStatus()).thenReturn(BikeStatus.AVAILABLE);

        when(userService.getUserByUUID(userId)).thenReturn(rider);
        when(bikeStationService.getStationById(bikeStationId)).thenReturn(station);
        when(bikeStationService.getStationBikes(bikeStationId)).thenReturn(Collections.singletonList(availableBike));
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Reservation result = reservationService.createReservation(userId, bikeStationId);

        // Assert
        long expiryMinutes = java.time.temporal.ChronoUnit.MINUTES.between(result.getStartTime(), result.getExpiry());
        assertTrue(expiryMinutes > 15, "Expiry should include loyalty bonus");
    }

    // ======================== getReservationById Tests ========================

    @Test
    @DisplayName("Should get reservation by ID successfully")
    void testGetReservationById() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = mock(Reservation.class);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        Reservation result = reservationService.getReservationById(reservationId);

        // Assert
        assertEquals(reservation, result);
    }

    @Test
    @DisplayName("Should throw RuntimeException when reservation not found")
    void testGetReservationByIdNotFound() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            reservationService.getReservationById(reservationId));
    }

    // ======================== getReservationsByUserId Tests ========================

    @Test
    @DisplayName("Should get all reservations for a specific user")
    void testGetReservationsByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<Reservation> reservations = Arrays.asList(
            mock(Reservation.class),
            mock(Reservation.class),
            mock(Reservation.class)
        );

        when(reservationRepository.findByUserId(userId)).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.getReservationsByUserId(userId);

        // Assert
        assertEquals(3, result.size());
        assertEquals(reservations, result);
    }

    @Test
    @DisplayName("Should return empty list when user has no reservations")
    void testGetReservationsByUserIdEmpty() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<Reservation> result = reservationService.getReservationsByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== getReservationsByBikeStation Tests ========================

    @Test
    @DisplayName("Should get all reservations at a specific bike station")
    void testGetReservationsByBikeStation() {
        // Arrange
        UUID bikeStationId = UUID.randomUUID();
        List<Reservation> reservations = Arrays.asList(
            mock(Reservation.class),
            mock(Reservation.class)
        );

        when(reservationRepository.findByBikeStationId(bikeStationId)).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.getReservationsByBikeStation(bikeStationId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(reservations, result);
    }

    @Test
    @DisplayName("Should return empty list when station has no reservations")
    void testGetReservationsByBikeStationEmpty() {
        // Arrange
        UUID bikeStationId = UUID.randomUUID();
        when(reservationRepository.findByBikeStationId(bikeStationId)).thenReturn(Collections.emptyList());

        // Act
        List<Reservation> result = reservationService.getReservationsByBikeStation(bikeStationId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== cancelReservation Tests ========================

    @Test
    @DisplayName("Should cancel a reservation successfully")
    void testCancelReservationSuccessfully() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        UUID bikeId = UUID.randomUUID();

        Reservation reservation = mock(Reservation.class);
        when(reservation.getBikeId()).thenReturn(bikeId);

        Bike bike = mock(Bike.class);
        when(bike.getId()).thenReturn(bikeId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(bikeService.getBikeById(bikeId)).thenReturn(bike);

        // Act
        reservationService.cancelReservation(reservationId);

        // Assert
        verify(bikeService).updateBikeStatus(bikeId, BikeStatus.AVAILABLE);
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("Should throw exception when canceling non-existent reservation")
    void testCancelReservationNotFound() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            reservationService.cancelReservation(reservationId));
    }

    // ======================== isReservationExpired Tests ========================

    @Test
    @DisplayName("Should return true when reservation is expired")
    void testIsReservationExpiredTrue() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = mock(Reservation.class);
        when(reservation.getExpiry()).thenReturn(LocalDateTime.now().minusMinutes(5));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        boolean result = reservationService.isReservationExpired(reservationId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when reservation is not expired")
    void testIsReservationExpiredFalse() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = mock(Reservation.class);
        when(reservation.getExpiry()).thenReturn(LocalDateTime.now().plusMinutes(30));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        boolean result = reservationService.isReservationExpired(reservationId);

        // Assert
        assertFalse(result);
    }

    // ======================== extendReservation Tests ========================

    @Test
    @DisplayName("Should extend a reservation successfully")
    void testExtendReservationSuccessfully() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        int additionalMinutes = 10;

        Reservation reservation = mock(Reservation.class);
        LocalDateTime originalExpiry = LocalDateTime.now().plusMinutes(30);
        when(reservation.getExpiry()).thenReturn(originalExpiry);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        // Act
        reservationService.extendReservation(reservationId, additionalMinutes);

        // Assert
        verify(reservation).setExpiry(originalExpiry.plusMinutes(additionalMinutes));
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Should throw exception when extending expired reservation")
    void testExtendExpiredReservation() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = mock(Reservation.class);
        when(reservation.getExpiry()).thenReturn(LocalDateTime.now().minusMinutes(5));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            reservationService.extendReservation(reservationId, 10));
    }

    @Test
    @DisplayName("Should extend reservation by exact number of minutes")
    void testExtendReservationByExactMinutes() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        int additionalMinutes = 25;

        Reservation reservation = mock(Reservation.class);
        LocalDateTime originalExpiry = LocalDateTime.now().plusMinutes(40);
        when(reservation.getExpiry()).thenReturn(originalExpiry);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        // Act
        reservationService.extendReservation(reservationId, additionalMinutes);

        // Assert
        verify(reservation).setExpiry(originalExpiry.plusMinutes(additionalMinutes));
    }

    // ======================== getAllActiveReservations Tests ========================

    @Test
    @DisplayName("Should get all active (non-expired) reservations")
    void testGetAllActiveReservations() {
        // Arrange
        Reservation activeReservation = mock(Reservation.class);
        UUID activeId = UUID.randomUUID();
        when(activeReservation.getId()).thenReturn(activeId);
        when(activeReservation.getExpiry()).thenReturn(LocalDateTime.now().plusMinutes(30));

        Reservation expiredReservation = mock(Reservation.class);
        UUID expiredId = UUID.randomUUID();
        when(expiredReservation.getId()).thenReturn(expiredId);
        when(expiredReservation.getExpiry()).thenReturn(LocalDateTime.now().minusMinutes(5));

        List<Reservation> allReservations = Arrays.asList(activeReservation, expiredReservation);
        when(reservationRepository.findAll()).thenReturn(allReservations);
        // Mock the repository to return the mocks when asked for by ID during filtering
        when(reservationRepository.findById(activeId)).thenReturn(Optional.of(activeReservation));
        when(reservationRepository.findById(expiredId)).thenReturn(Optional.of(expiredReservation));

        // Act
        List<Reservation> result = reservationService.getAllActiveReservations();

        // Assert
        assertEquals(1, result.size());
        assertEquals(activeReservation, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when all reservations expired")
    void testGetAllActiveReservationsAllExpired() {
        // Arrange
        Reservation expiredReservation1 = mock(Reservation.class);
        UUID expiredId1 = UUID.randomUUID();
        when(expiredReservation1.getId()).thenReturn(expiredId1);
        when(expiredReservation1.getExpiry()).thenReturn(LocalDateTime.now().minusMinutes(10));

        Reservation expiredReservation2 = mock(Reservation.class);
        UUID expiredId2 = UUID.randomUUID();
        when(expiredReservation2.getId()).thenReturn(expiredId2);
        when(expiredReservation2.getExpiry()).thenReturn(LocalDateTime.now().minusMinutes(5));

        List<Reservation> allExpired = Arrays.asList(expiredReservation1, expiredReservation2);
        when(reservationRepository.findAll()).thenReturn(allExpired);
        when(reservationRepository.findById(expiredId1)).thenReturn(Optional.of(expiredReservation1));
        when(reservationRepository.findById(expiredId2)).thenReturn(Optional.of(expiredReservation2));

        // Act
        List<Reservation> result = reservationService.getAllActiveReservations();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== updateReservation Tests ========================

    @Test
    @DisplayName("Should update reservation expiry")
    void testUpdateReservationExpiry() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation existingReservation = mock(Reservation.class);
        when(existingReservation.getId()).thenReturn(reservationId);

        Reservation updatedReservation = mock(Reservation.class);
        LocalDateTime newExpiry = LocalDateTime.now().plusHours(2);
        when(updatedReservation.getExpiry()).thenReturn(newExpiry);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(existingReservation)).thenReturn(existingReservation);

        // Act
        reservationService.updateReservation(reservationId, updatedReservation);

        // Assert
        verify(existingReservation).setExpiry(newExpiry);
        verify(reservationRepository).save(existingReservation);
    }

    @Test
    @DisplayName("Should not update expiry when update data has null expiry")
    void testUpdateReservationWithNullExpiry() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation existingReservation = mock(Reservation.class);
        when(existingReservation.getId()).thenReturn(reservationId);

        Reservation updatedReservation = mock(Reservation.class);
        when(updatedReservation.getExpiry()).thenReturn(null);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(existingReservation)).thenReturn(existingReservation);

        // Act
        reservationService.updateReservation(reservationId, updatedReservation);

        // Assert
        verify(existingReservation, never()).setExpiry(any());
    }

    // ======================== deleteReservation Tests ========================

    @Test
    @DisplayName("Should delete reservation by ID")
    void testDeleteReservation() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        // Act
        reservationService.deleteReservation(reservationId);

        // Assert
        verify(reservationRepository).deleteById(reservationId);
    }

    // ======================== getAllReservations Tests ========================

    @Test
    @DisplayName("Should get all reservations")
    void testGetAllReservations() {
        // Arrange
        List<Reservation> reservations = Arrays.asList(
            mock(Reservation.class),
            mock(Reservation.class),
            mock(Reservation.class),
            mock(Reservation.class)
        );

        when(reservationRepository.findAll()).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.getAllReservations();

        // Assert
        assertEquals(4, result.size());
        assertEquals(reservations, result);
    }

    @Test
    @DisplayName("Should return empty list when no reservations exist")
    void testGetAllReservationsEmpty() {
        // Arrange
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Reservation> result = reservationService.getAllReservations();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== getAllActiveReservationsForUser Tests ========================

    @Test
    @DisplayName("Should get all active reservations for a specific user")
    void testGetAllActiveReservationsForUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<Reservation> activeReservations = Arrays.asList(
            mock(Reservation.class),
            mock(Reservation.class)
        );

        when(reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.ACTIVE))
            .thenReturn(activeReservations);

        // Act
        List<Reservation> result = reservationService.getAllActiveReservationsForUser(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(activeReservations, result);
    }

    @Test
    @DisplayName("Should return empty list when user has no active reservations")
    void testGetAllActiveReservationsForUserEmpty() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.ACTIVE))
            .thenReturn(Collections.emptyList());

        // Act
        List<Reservation> result = reservationService.getAllActiveReservationsForUser(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== getExpiredReservationsPastYearByUserId Tests ========================

    @Test
    @DisplayName("Should get expired reservations from past year for user")
    void testGetExpiredReservationsPastYearByUserId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        Reservation expiredReservation = mock(Reservation.class);
        when(expiredReservation.getExpiry()).thenReturn(sixMonthsAgo);

        when(reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.EXPIRED))
            .thenReturn(Collections.singletonList(expiredReservation));

        // Act
        List<Reservation> result = reservationService.getExpiredReservationsPastYearByUserId(userId);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should filter out reservations older than one year")
    void testGetExpiredReservationsPastYearFiltersOldReservations() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);

        Reservation recentExpired = mock(Reservation.class);
        when(recentExpired.getExpiry()).thenReturn(sixMonthsAgo);

        Reservation oldExpired = mock(Reservation.class);
        when(oldExpired.getExpiry()).thenReturn(twoYearsAgo);

        List<Reservation> allExpired = Arrays.asList(recentExpired, oldExpired);
        when(reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.EXPIRED))
            .thenReturn(allExpired);

        // Act
        List<Reservation> result = reservationService.getExpiredReservationsPastYearByUserId(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(recentExpired, result.get(0));
    }

    // ======================== getReservationsPastYearByUserIdAndStatus Tests ========================

    @Test
    @DisplayName("Should get reservations with specific status from past year")
    void testGetReservationsPastYearByUserIdAndStatus() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ReservationStatus status = ReservationStatus.COMPLETED;
        LocalDateTime nineMonthsAgo = LocalDateTime.now().minusMonths(9);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getExpiry()).thenReturn(nineMonthsAgo);

        when(reservationRepository.findByUserIdAndStatus(userId, status))
            .thenReturn(Collections.singletonList(reservation));

        // Act
        List<Reservation> result = reservationService.getReservationsPastYearByUserIdAndStatus(userId, status);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should filter out reservations older than one year by status")
    void testGetReservationsPastYearByUserIdAndStatusFiltersOld() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ReservationStatus status = ReservationStatus.COMPLETED;
        LocalDateTime sevenMonthsAgo = LocalDateTime.now().minusMonths(7);
        LocalDateTime eighteenMonthsAgo = LocalDateTime.now().minusMonths(18);

        Reservation recent = mock(Reservation.class);
        when(recent.getExpiry()).thenReturn(sevenMonthsAgo);

        Reservation old = mock(Reservation.class);
        when(old.getExpiry()).thenReturn(eighteenMonthsAgo);

        when(reservationRepository.findByUserIdAndStatus(userId, status))
            .thenReturn(Arrays.asList(recent, old));

        // Act
        List<Reservation> result = reservationService.getReservationsPastYearByUserIdAndStatus(userId, status);

        // Assert
        assertEquals(1, result.size());
        assertEquals(recent, result.get(0));
    }

    // ======================== Integration Tests ========================

    @Test
    @DisplayName("Should create and cancel reservation in sequence")
    void testCreateAndCancelReservationSequence() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID bikeStationId = UUID.randomUUID();
        UUID bikeId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        Rider rider = mock(Rider.class);
        when(rider.getId()).thenReturn(userId);
        when(rider.getLoyaltyTier()).thenReturn(LoyaltyTier.SILVER);

        BikeStation station = mock(BikeStation.class);
        when(station.getReservationHoldTimeMinutes()).thenReturn(15);

        Bike availableBike = mock(Bike.class);
        when(availableBike.getId()).thenReturn(bikeId);
        when(availableBike.getStatus()).thenReturn(BikeStatus.AVAILABLE);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getBikeId()).thenReturn(bikeId);

        when(userService.getUserByUUID(userId)).thenReturn(rider);
        when(bikeStationService.getStationById(bikeStationId)).thenReturn(station);
        when(bikeStationService.getStationBikes(bikeStationId)).thenReturn(Collections.singletonList(availableBike));
        when(reservationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(bikeService.getBikeById(bikeId)).thenReturn(availableBike);

        // Act - Create reservation
        Reservation created = reservationService.createReservation(userId, bikeStationId);
        assertNotNull(created);

        // Act - Cancel reservation
        reservationService.cancelReservation(reservationId);

        // Assert
        verify(bikeService, times(1)).updateBikeStatus(bikeId, BikeStatus.RESERVED);
        verify(bikeService, times(1)).updateBikeStatus(bikeId, BikeStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should extend reservation before expiry")
    void testExtendReservationBeforeExpiry() {
        // Arrange
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = mock(Reservation.class);
        LocalDateTime originalExpiry = LocalDateTime.now().plusMinutes(20);
        when(reservation.getExpiry()).thenReturn(originalExpiry);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        // Act
        reservationService.extendReservation(reservationId, 15);

        // Assert
        verify(reservation).setExpiry(originalExpiry.plusMinutes(15));
    }

    @Test
    @DisplayName("Should handle multiple reservations for same station")
    void testMultipleReservationsForSameStation() {
        // Arrange
        UUID stationId = UUID.randomUUID();
        List<Reservation> stationReservations = Arrays.asList(
            mock(Reservation.class),
            mock(Reservation.class),
            mock(Reservation.class)
        );

        when(reservationRepository.findByBikeStationId(stationId)).thenReturn(stationReservations);

        // Act
        List<Reservation> result = reservationService.getReservationsByBikeStation(stationId);

        // Assert
        assertEquals(3, result.size());
    }
}
