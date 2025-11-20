package bigcie.bigcie.assemblers.facades;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.CreditCardType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.mappers.PaymentInfoMapper;
import bigcie.bigcie.mappers.TripMapper;
import bigcie.bigcie.services.read.interfaces.IBikeStationLookup;
import bigcie.bigcie.services.read.interfaces.IPaymentLookup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TripAssembler.class, TripMapper.class, PaymentInfoMapper.class})
class TripAssemblerIntegrationTest {

    @MockBean
    private IBikeStationLookup bikeStationLookup;

    @MockBean
    private IPaymentLookup paymentLookup;

    @Autowired
    private TripAssembler tripAssembler;

    @Test
    void enrichTripDtosWithStationNamesPaymentInfoAndCaching() {
        UUID userId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID startStationId = UUID.randomUUID();
        UUID endStationId = UUID.randomUUID();
        UUID paymentInfoId = UUID.randomUUID();

        Trip trip = new Trip.Builder()
                .id(tripId)
                .userId(userId)
                .bikeId(UUID.randomUUID())
                .bikeStationStartId(startStationId)
                .bikeStationEndId(endStationId)
                .startDate(LocalDateTime.now().minusMinutes(30))
                .endDate(LocalDateTime.now())
                .status(TripStatus.COMPLETED)
                .pricingPlan(PricingPlan.SINGLE_RIDE)
                .bikeType(BikeType.STANDARD)
                .paymentInfoId(paymentInfoId)
                .build();
        trip.setCost(5.50);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setId(paymentInfoId);
        paymentInfo.setCreditCardNumber("4000000000001234");
        paymentInfo.setCardHolderName("Alex Rider");
        paymentInfo.setCardExpiry("12/30");
        paymentInfo.setCardType(CreditCardType.VISA);
        paymentInfo.setDefault(true);

        when(bikeStationLookup.getStationNameById(startStationId)).thenReturn("Start Station");
        when(bikeStationLookup.getStationNameById(endStationId)).thenReturn("End Station");
        when(paymentLookup.getPaymentInfo(paymentInfoId, userId)).thenReturn(paymentInfo);

        List<TripDto> dtos = tripAssembler.enrichTripDtoList(List.of(trip), userId);

        TripDto dto = dtos.getFirst();
        assertThat(dto.getBikeStationStart()).isEqualTo("Start Station");
        assertThat(dto.getBikeStationEnd()).isEqualTo("End Station");
        assertThat(dto.getPaymentInfo()).isNotNull();
        assertThat(dto.getPaymentInfo().getLastFourCreditCardNumber()).isEqualTo("1234");
        assertThat(dto.getPaymentInfo().getPaymentInfoId()).isEqualTo(paymentInfoId);

        
        tripAssembler.enrichTripDtoList(List.of(trip), userId);
        verify(bikeStationLookup, times(1)).getStationNameById(startStationId);
        verify(bikeStationLookup, times(1)).getStationNameById(endStationId);

        tripAssembler.clearCache();
        tripAssembler.enrichTripDtoList(List.of(trip), userId);
        verify(bikeStationLookup, times(2)).getStationNameById(startStationId);
        verify(bikeStationLookup, times(2)).getStationNameById(endStationId);
    }
}
