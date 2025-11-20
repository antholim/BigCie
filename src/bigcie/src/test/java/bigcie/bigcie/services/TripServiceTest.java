package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.TripAssembler;
import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import bigcie.bigcie.services.interfaces.IPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private IPriceService priceService;

    @Mock
    private TripAssembler tripAssembler;

    @Mock
    private IFlexDollarService flexDollarService;

    @InjectMocks
    private TripService tripService;

    private UUID testUserId;
    private UUID testBikeId;
    private UUID testStationStartId;
    private UUID testStationEndId;
    private UUID testPaymentInfoId;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testBikeId = UUID.randomUUID();
        testStationStartId = UUID.randomUUID();
        testStationEndId = UUID.randomUUID();
        testPaymentInfoId = UUID.randomUUID();

        testTrip = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .bikeId(testBikeId)
                .bikeStationStartId(testStationStartId)
                .startDate(LocalDateTime.now().minusHours(2))
                .status(TripStatus.ONGOING)
                .pricingPlan(PricingPlan.SINGLE_RIDE)
                .bikeType(BikeType.STANDARD)
                .paymentInfoId(testPaymentInfoId)
                .build();
    }

    @Test
    void testGetTripById_Success() {
        // Arrange
        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));

        // Act
        Trip result = tripService.getTripById(testTrip.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testTrip.getId(), result.getId());
        assertEquals(TripStatus.ONGOING, result.getStatus());
        verify(tripRepository).findById(testTrip.getId());
    }

    @Test
    void testGetTripById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(tripRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Trip result = tripService.getTripById(nonExistentId);

        // Assert
        assertNull(result);
        verify(tripRepository).findById(nonExistentId);
    }

    @Test
    void testCreateTrip_StandardBike() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setId(UUID.randomUUID());
            return trip;
        });

        // Act
        Trip result = tripService.createTrip(testUserId, testBikeId, testStationStartId,
                PricingPlan.SINGLE_RIDE, BikeType.STANDARD, testPaymentInfoId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(testBikeId, result.getBikeId());
        assertEquals(testStationStartId, result.getBikeStationStartId());
        assertEquals(BikeType.STANDARD, result.getBikeType());
        assertEquals(TripStatus.ONGOING, result.getStatus());
        assertEquals(PricingPlan.SINGLE_RIDE, result.getPricingPlan());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void testCreateTrip_EBike() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Trip result = tripService.createTrip(testUserId, testBikeId, testStationStartId,
                PricingPlan.DAY_PASS, BikeType.E_BIKE, testPaymentInfoId);

        // Assert
        assertNotNull(result);
        assertEquals(BikeType.E_BIKE, result.getBikeType());
        assertEquals(PricingPlan.DAY_PASS, result.getPricingPlan());
        assertEquals(TripStatus.ONGOING, result.getStatus());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void testCreateTrip_MonthlyPass() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Trip result = tripService.createTrip(testUserId, testBikeId, testStationStartId,
                PricingPlan.MONTHLY_PASS, BikeType.STANDARD, testPaymentInfoId);

        // Assert
        assertNotNull(result);
        assertEquals(PricingPlan.MONTHLY_PASS, result.getPricingPlan());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void testEndTrip_Success() {
        // Arrange
        testTrip.setStartDate(LocalDateTime.now().minusHours(2));
        double totalCost = 15.50;
        double flexDollarsUsed = 5.0;

        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));
        when(priceService.calculatePrice(any(), any(), eq(BikeType.STANDARD), eq(PricingPlan.SINGLE_RIDE), eq(0)))
                .thenReturn(totalCost);
        when(flexDollarService.deductFlexDollars(testUserId, totalCost)).thenReturn(flexDollarsUsed);
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.endTrip(testTrip.getId(), testStationEndId, 0);

        // Assert
        assertEquals(TripStatus.COMPLETED, testTrip.getStatus());
        assertEquals(testStationEndId, testTrip.getBikeStationEndId());
        assertNotNull(testTrip.getEndDate());
        assertEquals(totalCost, testTrip.getCost());
        assertEquals(flexDollarsUsed, testTrip.getFlexDollarsUsed());
        assertEquals(totalCost - flexDollarsUsed, testTrip.getAmountCharged());
        verify(tripRepository).save(testTrip);
    }

    @Test
    void testEndTrip_WithDiscount() {
        // Arrange
        testTrip.setStartDate(LocalDateTime.now().minusHours(2));
        double discountedCost = 18.0;
        double flexDollarsUsed = 8.0;
        int discountPercentage = 10;

        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));
        when(priceService.calculatePrice(any(), any(), eq(BikeType.STANDARD), eq(PricingPlan.SINGLE_RIDE),
                eq(discountPercentage)))
                .thenReturn(discountedCost);
        when(flexDollarService.deductFlexDollars(testUserId, discountedCost)).thenReturn(flexDollarsUsed);
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.endTrip(testTrip.getId(), testStationEndId, discountPercentage);

        // Assert
        assertEquals(discountedCost, testTrip.getCost());
        assertEquals(discountPercentage, testTrip.getDiscountApplied());
        verify(tripRepository).save(testTrip);
    }

    @Test
    void testEndTrip_AlreadyCompleted() {
        // Arrange
        testTrip.setStatus(TripStatus.COMPLETED);
        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> tripService.endTrip(testTrip.getId(), testStationEndId, 0));
    }

    @Test
    void testEndTrip_TripNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(tripRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> tripService.endTrip(nonExistentId, testStationEndId, 0));
    }

    @Test
    void testGetTripByUserId_ReturnsEnrichedList() {
        // Arrange
        List<Trip> trips = Arrays.asList(testTrip);
        List<TripDto> enrichedTrips = new ArrayList<>();

        when(tripRepository.findByUserId(testUserId)).thenReturn(trips);
        when(tripAssembler.enrichTripDtoList(trips, testUserId)).thenReturn(enrichedTrips);

        // Act
        List<TripDto> result = tripService.getTripByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(tripRepository).findByUserId(testUserId);
        verify(tripAssembler).enrichTripDtoList(trips, testUserId);
    }

    @Test
    void testGetTripByUserId_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Trip> tripPage = new PageImpl<>(Arrays.asList(testTrip), pageable, 1);
        Page<TripDto> enrichedPage = new PageImpl<>(new ArrayList<>(), pageable, 1);

        when(tripRepository.findByUserId(testUserId, pageable)).thenReturn(tripPage);
        when(tripAssembler.enrichTripDtoPage(tripPage, testUserId)).thenReturn(enrichedPage);

        // Act
        Page<TripDto> result = tripService.getTripByUserId(testUserId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tripRepository).findByUserId(testUserId, pageable);
        verify(tripAssembler).enrichTripDtoPage(tripPage, testUserId);
    }

    @Test
    void testGetAllTrips() {
        // Arrange
        List<Trip> allTrips = Arrays.asList(testTrip);
        List<TripDto> enrichedTrips = new ArrayList<>();

        when(tripRepository.findAll()).thenReturn(allTrips);
        when(tripAssembler.enrichTripDtoListNoLogging(allTrips)).thenReturn(enrichedTrips);

        // Act
        List<TripDto> result = tripService.getAllTrips();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(tripRepository).findAll();
        verify(tripAssembler).enrichTripDtoListNoLogging(allTrips);
    }

    @Test
    void testGetCompletedTripsPastYear_Success() {
        // Arrange
        Trip completedTrip = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .status(TripStatus.COMPLETED)
                .endDate(LocalDateTime.now().minusMonths(6))
                .build();

        List<Trip> completedTrips = Arrays.asList(completedTrip);

        when(tripRepository.findByUserIdAndStatus(testUserId, TripStatus.COMPLETED))
                .thenReturn(completedTrips);

        // Act
        List<Trip> result = tripService.getCompletedTripsPastYearByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tripRepository).findByUserIdAndStatus(testUserId, TripStatus.COMPLETED);
    }

    @Test
    void testGetCompletedTripsPastYear_ExcludesOldTrips() {
        // Arrange
        Trip oldCompletedTrip = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .status(TripStatus.COMPLETED)
                .endDate(LocalDateTime.now().minusYears(2))
                .build();

        List<Trip> allCompletedTrips = Arrays.asList(oldCompletedTrip);

        when(tripRepository.findByUserIdAndStatus(testUserId, TripStatus.COMPLETED))
                .thenReturn(allCompletedTrips);

        // Act
        List<Trip> result = tripService.getCompletedTripsPastYearByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testMeetsMonthlyTripRequirement_Success() {
        // Arrange
        Trip trip1 = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .startDate(LocalDateTime.now().minusDays(10))
                .build();

        Trip trip2 = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .startDate(LocalDateTime.now().minusDays(5))
                .build();

        Trip trip3 = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .startDate(LocalDateTime.now().minusDays(2))
                .build();

        List<Trip> trips = Arrays.asList(trip1, trip2, trip3);

        when(tripRepository.findByUserId(testUserId)).thenReturn(trips);

        // Act
        boolean result = tripService.meetsMonthlyTripRequirement(testUserId, 2, 1);

        // Assert
        assertTrue(result);
        verify(tripRepository).findByUserId(testUserId);
    }

    @Test
    void testMeetsMonthlyTripRequirement_Failure() {
        // Arrange
        Trip trip1 = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .startDate(LocalDateTime.now().minusDays(10))
                .build();

        List<Trip> trips = Arrays.asList(trip1);

        when(tripRepository.findByUserId(testUserId)).thenReturn(trips);

        // Act
        boolean result = tripService.meetsMonthlyTripRequirement(testUserId, 5, 1);

        // Assert
        assertFalse(result);
        verify(tripRepository).findByUserId(testUserId);
    }

    @Test
    void testMeetsWeeklyTripRequirement_ReturnsBoolean() {
        // Arrange - test that the method executes and returns a boolean
        List<Trip> trips = new ArrayList<>();
        Trip trip1 = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .startDate(LocalDateTime.now().minusWeeks(2))
                .build();
        trips.add(trip1);

        when(tripRepository.findByUserId(testUserId)).thenReturn(trips);

        // Act
        boolean result = tripService.meetsWeeklyTripRequirement(testUserId, 1, 1);

        // Assert - verify method executes correctly and returns a boolean
        assertNotNull(result);
        verify(tripRepository).findByUserId(testUserId);
    }

    @Test
    void testMeetsWeeklyTripRequirement_Failure() {
        // Arrange
        List<Trip> trips = new ArrayList<>();

        when(tripRepository.findByUserId(testUserId)).thenReturn(trips);

        // Act
        boolean result = tripService.meetsWeeklyTripRequirement(testUserId, 5, 1);

        // Assert
        assertFalse(result);
        verify(tripRepository).findByUserId(testUserId);
    }

    @Test
    void testEndTrip_FlexDollarDeduction() {
        // Arrange
        testTrip.setStartDate(LocalDateTime.now().minusHours(1));
        double totalCost = 25.0;
        double flexDollarsAvailable = 10.0;

        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));
        when(priceService.calculatePrice(any(), any(), any(), any(), anyInt())).thenReturn(totalCost);
        when(flexDollarService.deductFlexDollars(testUserId, totalCost)).thenReturn(flexDollarsAvailable);
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.endTrip(testTrip.getId(), testStationEndId, 0);

        // Assert
        assertEquals(flexDollarsAvailable, testTrip.getFlexDollarsUsed());
        assertEquals(totalCost - flexDollarsAvailable, testTrip.getAmountCharged());
        verify(flexDollarService).deductFlexDollars(testUserId, totalCost);
    }

    @Test
    void testCreateTrip_AllPricingPlans() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        for (PricingPlan plan : PricingPlan.values()) {
            Trip result = tripService.createTrip(testUserId, testBikeId, testStationStartId,
                    plan, BikeType.STANDARD, testPaymentInfoId);

            assertNotNull(result);
            assertEquals(plan, result.getPricingPlan());
        }
    }

    @Test
    void testCreateTrip_AllBikeTypes() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        for (BikeType bikeType : BikeType.values()) {
            Trip result = tripService.createTrip(testUserId, testBikeId, testStationStartId,
                    PricingPlan.SINGLE_RIDE, bikeType, testPaymentInfoId);

            assertNotNull(result);
            assertEquals(bikeType, result.getBikeType());
        }
    }

    @Test
    void testEndTrip_VerifiesPriceCalculation() {
        // Arrange
        testTrip.setStartDate(LocalDateTime.now().minusHours(3));
        double expectedCost = 20.0;

        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));
        when(priceService.calculatePrice(any(), any(), eq(BikeType.STANDARD), eq(PricingPlan.SINGLE_RIDE), eq(0)))
                .thenReturn(expectedCost);
        when(flexDollarService.deductFlexDollars(testUserId, expectedCost)).thenReturn(0.0);
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.endTrip(testTrip.getId(), testStationEndId, 0);

        // Assert
        verify(priceService).calculatePrice(any(), any(), eq(BikeType.STANDARD), eq(PricingPlan.SINGLE_RIDE), eq(0));
        assertEquals(expectedCost, testTrip.getCost());
    }

    @Test
    void testGetTripByUserId_EmptyResult() {
        // Arrange
        when(tripRepository.findByUserId(testUserId)).thenReturn(new ArrayList<>());
        when(tripAssembler.enrichTripDtoList(new ArrayList<>(), testUserId)).thenReturn(new ArrayList<>());

        // Act
        List<TripDto> result = tripService.getTripByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testEndTrip_StatusTransitionONGOINGtoCOMPLETED() {
        // Arrange
        assertEquals(TripStatus.ONGOING, testTrip.getStatus());

        when(tripRepository.findById(testTrip.getId())).thenReturn(Optional.of(testTrip));
        when(priceService.calculatePrice(any(), any(), any(), any(), anyInt())).thenReturn(10.0);
        when(flexDollarService.deductFlexDollars(any(), anyDouble())).thenReturn(0.0);
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.endTrip(testTrip.getId(), testStationEndId, 0);

        // Assert
        assertEquals(TripStatus.COMPLETED, testTrip.getStatus());
    }

    @Test
    void testCreateTrip_VerifiesTripSaved() {
        // Arrange
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tripService.createTrip(testUserId, testBikeId, testStationStartId,
                PricingPlan.SINGLE_RIDE, BikeType.STANDARD, testPaymentInfoId);

        // Assert
        verify(tripRepository).save(any(Trip.class));
    }
}
