package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.TripAssembler;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IPriceService;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TripService.class)
class TripServiceIntegrationTest {

    @MockBean
    private TripRepository tripRepository;

    @MockBean
    private IPriceService priceService;

    @MockBean
    private TripAssembler tripAssembler;

    @MockBean
    private IFlexDollarService flexDollarService;

    @Autowired
    private TripService tripService;

    @Test
    void endTripCalculatesCostAppliesDiscountAndFlexDollars() {
        UUID tripId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID startStation = UUID.randomUUID();
        UUID endStation = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(20);

        Trip trip = new Trip.Builder()
                .id(tripId)
                .userId(userId)
                .bikeId(UUID.randomUUID())
                .bikeStationStartId(startStation)
                .startDate(startTime)
                .status(TripStatus.ONGOING)
                .pricingPlan(PricingPlan.SINGLE_RIDE)
                .bikeType(BikeType.STANDARD)
                .paymentInfoId(UUID.randomUUID())
                .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(priceService.calculatePrice(any(), any(), any(), any(), any(int.class))).thenReturn(12.75);
        when(flexDollarService.deductFlexDollars(userId, 12.75)).thenReturn(2.75);
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tripService.endTrip(tripId, endStation, 10);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(tripCaptor.capture());
        Trip savedTrip = tripCaptor.getValue();

        assertThat(savedTrip.getBikeStationEndId()).isEqualTo(endStation);
        assertThat(savedTrip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(savedTrip.getDiscountApplied()).isEqualTo(10);
        assertThat(savedTrip.getCost()).isEqualTo(12.75);
        assertThat(savedTrip.getFlexDollarsUsed()).isEqualTo(2.75);
        assertThat(savedTrip.getAmountCharged()).isEqualTo(10.0);
    }
}
