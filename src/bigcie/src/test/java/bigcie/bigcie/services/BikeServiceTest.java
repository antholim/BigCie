package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BikeServiceTest {

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private BikeStationService bikeStationService;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private BikeService bikeService;

    private Bike testBike;
    private UUID testBikeId;
    private UUID testStationId;
    private BikeStation testStation;
    private BikeRequest testBikeRequest;

    @BeforeEach
    void setUp() {
        testBikeId = UUID.randomUUID();
        testStationId = UUID.randomUUID();

        testBike = new Bike();
        testBike.setId(testBikeId);
        testBike.setBikeType(BikeType.STANDARD);
        testBike.setStatus(BikeStatus.AVAILABLE);

        testStation = new BikeStation();
        testStation.setId(testStationId);
        testStation.setBikesIds(new ArrayList<>());
        testStation.setStandardBikesDocked(0);
        testStation.setEBikesDocked(0);

        testBikeRequest = new BikeRequest();
        testBikeRequest.setBikeStationId(testStationId);
        testBikeRequest.setBikeType(BikeType.STANDARD);
        testBikeRequest.setStatus(BikeStatus.AVAILABLE);
    }

    @Test
    void testCreateBike_StandardBike_Success() {
        // Arrange
        when(bikeStationService.getStationById(testStationId)).thenReturn(testStation);
        when(bikeRepository.save(any(Bike.class))).thenReturn(testBike);
        when(bikeRepository.findAllById(any())).thenReturn(List.of(testBike));
        when(bikeStationService.updateStation(eq(testStationId), any())).thenReturn(testStation);

        // Act
        Bike createdBike = bikeService.createBike(testBikeRequest);

        // Assert
        assertNotNull(createdBike);
        assertEquals(BikeType.STANDARD, createdBike.getBikeType());
        assertEquals(BikeStatus.AVAILABLE, createdBike.getStatus());
        verify(bikeRepository).save(any(Bike.class));
        verify(bikeStationService).updateStation(eq(testStationId), any(BikeStation.class));
    }

    @Test
    void testCreateBike_EBike_Success() {
        // Arrange
        testBikeRequest.setBikeType(BikeType.E_BIKE);
        testBike.setBikeType(BikeType.E_BIKE);
        when(bikeStationService.getStationById(testStationId)).thenReturn(testStation);
        when(bikeRepository.save(any(Bike.class))).thenReturn(testBike);
        when(bikeRepository.findAllById(any())).thenReturn(List.of(testBike));
        when(bikeStationService.updateStation(eq(testStationId), any())).thenReturn(testStation);

        // Act
        Bike createdBike = bikeService.createBike(testBikeRequest);

        // Assert
        assertNotNull(createdBike);
        assertEquals(BikeType.E_BIKE, createdBike.getBikeType());
        verify(bikeRepository).save(any(Bike.class));
    }

    @Test
    void testGetBikeById_Success() {
        // Arrange
        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));

        // Act
        Bike foundBike = bikeService.getBikeById(testBikeId);

        // Assert
        assertNotNull(foundBike);
        assertEquals(testBikeId, foundBike.getId());
        assertEquals(BikeType.STANDARD, foundBike.getBikeType());
    }

    @Test
    void testGetBikeById_NotFound() {
        // Arrange
        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bikeService.getBikeById(testBikeId));
        verify(bikeRepository).findById(testBikeId);
    }

    @Test
    void testGetAllBikes_Success() {
        // Arrange
        Bike bike1 = new Bike();
        bike1.setId(UUID.randomUUID());
        Bike bike2 = new Bike();
        bike2.setId(UUID.randomUUID());

        when(bikeRepository.findAll()).thenReturn(List.of(bike1, bike2));

        // Act
        List<Bike> bikes = bikeService.getAllBikes();

        // Assert
        assertNotNull(bikes);
        assertEquals(2, bikes.size());
        verify(bikeRepository).findAll();
    }

    @Test
    void testGetAllBikes_Empty() {
        // Arrange
        when(bikeRepository.findAll()).thenReturn(List.of());

        // Act
        List<Bike> bikes = bikeService.getAllBikes();

        // Assert
        assertNotNull(bikes);
        assertEquals(0, bikes.size());
    }

    @ParameterizedTest
    @EnumSource(BikeStatus.class)
    void testGetBikesByStatus_Success(BikeStatus status) {
        // Arrange
        testBike.setStatus(status);
        when(bikeRepository.findByStatus(status)).thenReturn(List.of(testBike));

        // Act
        List<Bike> bikes = bikeService.getBikesByStatus(status);

        // Assert
        assertNotNull(bikes);
        assertEquals(1, bikes.size());
        assertEquals(status, bikes.get(0).getStatus());
    }

    @Test
    void testGetBikesByStatus_Empty() {
        // Arrange
        when(bikeRepository.findByStatus(BikeStatus.AVAILABLE)).thenReturn(List.of());

        // Act
        List<Bike> bikes = bikeService.getBikesByStatus(BikeStatus.AVAILABLE);

        // Assert
        assertNotNull(bikes);
        assertEquals(0, bikes.size());
    }

    @Test
    void testUpdateBike_ChangeStatus() {
        // Arrange
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.RESERVED);
        updatedBike.setBikeType(BikeType.STANDARD);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBike(testBikeId, updatedBike);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.RESERVED, result.getStatus());
        verify(notificationService).notifyBikeStatusChange(testBikeId, BikeStatus.RESERVED);
        verify(bikeRepository).save(any(Bike.class));
    }

    @Test
    void testUpdateBikeStatus_FromAvailableToReserved() {
        // Arrange
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.RESERVED);
        updatedBike.setBikeType(BikeType.STANDARD);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBikeStatus(testBikeId, BikeStatus.RESERVED);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.RESERVED, result.getStatus());
        verify(notificationService).notifyBikeStatusChange(testBikeId, BikeStatus.RESERVED);
    }

    @Test
    void testUpdateBikeStatus_FromAvailableToOnTrip() {
        // Arrange
        testBike.setStatus(BikeStatus.AVAILABLE);
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.ON_TRIP);
        updatedBike.setBikeType(BikeType.STANDARD);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBikeStatus(testBikeId, BikeStatus.ON_TRIP);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.ON_TRIP, result.getStatus());
        verify(notificationService).notifyBikeStatusChange(testBikeId, BikeStatus.ON_TRIP);
        verify(notificationService).publishTripEvent(any());
    }

    @Test
    void testUpdateBikeStatus_FromOnTripToAvailable() {
        // Arrange
        testBike.setStatus(BikeStatus.ON_TRIP);
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.AVAILABLE);
        updatedBike.setBikeType(BikeType.STANDARD);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBikeStatus(testBikeId, BikeStatus.AVAILABLE);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.AVAILABLE, result.getStatus());
        verify(notificationService).publishTripEvent(any());
    }

    @Test
    void testUpdateBikeStatus_SameStatus_NoEvent() {
        // Arrange
        testBike.setStatus(BikeStatus.AVAILABLE);
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.AVAILABLE);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBikeStatus(testBikeId, BikeStatus.AVAILABLE);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.AVAILABLE, result.getStatus());
        // Verify trip event is NOT published when status doesn't change
        verify(notificationService, never()).publishTripEvent(any());
    }

    @Test
    void testDeleteBike_Success() {
        // Act
        bikeService.deleteBike(testBikeId);

        // Assert
        verify(bikeRepository).deleteById(testBikeId);
    }

    @Test
    void testBulkCreateBikes_Success() {
        // Arrange
        BikeRequest request1 = new BikeRequest();
        request1.setBikeStationId(testStationId);
        request1.setBikeType(BikeType.STANDARD);
        request1.setStatus(BikeStatus.AVAILABLE);

        BikeRequest request2 = new BikeRequest();
        request2.setBikeStationId(testStationId);
        request2.setBikeType(BikeType.E_BIKE);
        request2.setStatus(BikeStatus.AVAILABLE);

        Bike bike1 = new Bike();
        bike1.setId(UUID.randomUUID());
        bike1.setBikeType(BikeType.STANDARD);

        Bike bike2 = new Bike();
        bike2.setId(UUID.randomUUID());
        bike2.setBikeType(BikeType.E_BIKE);

        when(bikeStationService.getStationById(testStationId)).thenReturn(testStation);
        when(bikeRepository.save(any(Bike.class))).thenReturn(bike1).thenReturn(bike2);
        when(bikeRepository.findAllById(any())).thenReturn(List.of(bike1, bike2));
        when(bikeStationService.updateStation(eq(testStationId), any())).thenReturn(testStation);

        // Act
        List<Bike> createdBikes = bikeService.bulkCreateBikes(List.of(request1, request2));

        // Assert
        assertNotNull(createdBikes);
        assertEquals(2, createdBikes.size());
        verify(bikeRepository, times(2)).save(any(Bike.class));
    }

    @Test
    void testGetBikeIdFromRiderId_RiderWithBikes() {
        // Arrange
        UUID riderId = UUID.randomUUID();
        Rider rider = new Rider();
        UUID bike1Id = UUID.randomUUID();
        UUID bike2Id = UUID.randomUUID();
        rider.setCurrentBikes(List.of(bike1Id, bike2Id));

        when(userService.getUserByUUID(riderId)).thenReturn(rider);

        // Act
        List<UUID> bikeIds = bikeService.getBikeIdFromRiderId(riderId);

        // Assert
        assertNotNull(bikeIds);
        assertEquals(2, bikeIds.size());
        assertTrue(bikeIds.contains(bike1Id));
        assertTrue(bikeIds.contains(bike2Id));
    }

    @Test
    void testGetBikeIdFromRiderId_RiderWithoutBikes() {
        // Arrange
        UUID riderId = UUID.randomUUID();
        Rider rider = new Rider();
        rider.setCurrentBikes(new ArrayList<>());

        when(userService.getUserByUUID(riderId)).thenReturn(rider);

        // Act
        List<UUID> bikeIds = bikeService.getBikeIdFromRiderId(riderId);

        // Assert
        assertNotNull(bikeIds);
        assertEquals(0, bikeIds.size());
    }

    @Test
    void testUpdateBike_ChangeBikeType() {
        // Arrange
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.AVAILABLE);
        updatedBike.setBikeType(BikeType.E_BIKE);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBike(testBikeId, updatedBike);

        // Assert
        assertNotNull(result);
        assertEquals(BikeType.E_BIKE, result.getBikeType());
        verify(bikeRepository).save(any(Bike.class));
    }

    @Test
    void testCreateBike_WithReservationExpiry() {
        // Arrange
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);
        testBikeRequest.setReservationExpiry(expiryTime);
        testBike.setReservationExpiry(expiryTime);

        when(bikeStationService.getStationById(testStationId)).thenReturn(testStation);
        when(bikeRepository.save(any(Bike.class))).thenReturn(testBike);
        when(bikeRepository.findAllById(any())).thenReturn(List.of(testBike));
        when(bikeStationService.updateStation(eq(testStationId), any())).thenReturn(testStation);

        // Act
        Bike createdBike = bikeService.createBike(testBikeRequest);

        // Assert
        assertNotNull(createdBike);
        assertEquals(expiryTime, createdBike.getReservationExpiry());
    }

    @Test
    void testGetBikesByStatus_MultipleResults() {
        // Arrange
        Bike bike1 = new Bike();
        bike1.setId(UUID.randomUUID());
        bike1.setStatus(BikeStatus.AVAILABLE);

        Bike bike2 = new Bike();
        bike2.setId(UUID.randomUUID());
        bike2.setStatus(BikeStatus.AVAILABLE);

        when(bikeRepository.findByStatus(BikeStatus.AVAILABLE)).thenReturn(List.of(bike1, bike2));

        // Act
        List<Bike> bikes = bikeService.getBikesByStatus(BikeStatus.AVAILABLE);

        // Assert
        assertNotNull(bikes);
        assertEquals(2, bikes.size());
    }

    @Test
    void testUpdateBikeStatus_ToMaintenance() {
        // Arrange
        testBike.setStatus(BikeStatus.AVAILABLE);
        Bike updatedBike = new Bike();
        updatedBike.setId(testBikeId);
        updatedBike.setStatus(BikeStatus.MAINTENANCE);

        when(bikeRepository.findById(testBikeId)).thenReturn(Optional.of(testBike));
        when(bikeRepository.save(any(Bike.class))).thenReturn(updatedBike);

        // Act
        Bike result = bikeService.updateBikeStatus(testBikeId, BikeStatus.MAINTENANCE);

        // Assert
        assertNotNull(result);
        assertEquals(BikeStatus.MAINTENANCE, result.getStatus());
        verify(notificationService).notifyBikeStatusChange(testBikeId, BikeStatus.MAINTENANCE);
    }
}
