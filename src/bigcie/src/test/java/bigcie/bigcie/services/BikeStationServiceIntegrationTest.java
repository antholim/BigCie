package bigcie.bigcie.services;

import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStationStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import bigcie.bigcie.repositories.ReservationRepository;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BikeStationService.class)
class BikeStationServiceIntegrationTest {

    @MockBean
    private BikeRepository bikeRepository;

    @MockBean
    private BikeStationRepository bikeStationRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private INotificationService notificationService;

    @MockBean
    private IUserService userService;

    @MockBean
    private ITripService tripService;

    @MockBean
    private IFlexDollarService flexDollarService;

    @MockBean
    private LoyaltyTierContext loyaltyTierContext;

    @Autowired
    private BikeStationService bikeStationService;

    @Test
    void rebalanceBikesEvenlyDistributesInventoryAndUpdatesStatuses() {
        UUID stationAId = UUID.randomUUID();
        UUID stationBId = UUID.randomUUID();

        UUID bike1 = UUID.randomUUID();
        UUID bike2 = UUID.randomUUID();
        UUID bike3 = UUID.randomUUID();
        UUID bike4 = UUID.randomUUID();

        BikeStation stationA = new BikeStation();
        stationA.setId(stationAId);
        stationA.setCapacity(3);
        stationA.setStatus(BikeStationStatus.OCCUPIED);
        stationA.setBikesIds(new ArrayList<>(List.of(bike1, bike2, bike3)));
        stationA.setStandardBikesDocked(2);
        stationA.setEBikesDocked(1);

        BikeStation stationB = new BikeStation();
        stationB.setId(stationBId);
        stationB.setCapacity(3);
        stationB.setStatus(BikeStationStatus.EMPTY);
        stationB.setBikesIds(new ArrayList<>(List.of(bike4)));
        stationB.setStandardBikesDocked(1);
        stationB.setEBikesDocked(0);

        when(bikeStationRepository.findAll()).thenReturn(List.of(stationA, stationB));
        when(bikeRepository.findBikeById(bike1)).thenReturn(bikeOfType(bike1, BikeType.STANDARD));
        when(bikeRepository.findBikeById(bike2)).thenReturn(bikeOfType(bike2, BikeType.E_BIKE));
        when(bikeRepository.findBikeById(bike3)).thenReturn(bikeOfType(bike3, BikeType.STANDARD));
        when(bikeRepository.findBikeById(bike4)).thenReturn(bikeOfType(bike4, BikeType.STANDARD));
        when(bikeStationRepository.save(any(BikeStation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bikeStationService.rebalanceBikes();

        ArgumentCaptor<BikeStation> captor = ArgumentCaptor.forClass(BikeStation.class);
        verify(bikeStationRepository, atLeast(2)).save(captor.capture());

        Map<UUID, BikeStation> finalStations = captor.getAllValues().stream()
                // keep the last saved version for each station
                .collect(Collectors.toMap(BikeStation::getId, s -> s, (first, second) -> second));

        BikeStation finalA = finalStations.get(stationAId);
        BikeStation finalB = finalStations.get(stationBId);

        assertThat(finalA.getBikesIds()).hasSize(2);
        assertThat(finalB.getBikesIds()).hasSize(2);
        assertThat(finalA.getStandardBikesDocked() + finalA.getEBikesDocked()).isEqualTo(2);
        assertThat(finalB.getStandardBikesDocked() + finalB.getEBikesDocked()).isEqualTo(2);
        assertThat(finalA.getStatus()).isEqualTo(BikeStationStatus.OCCUPIED);
        assertThat(finalB.getStatus()).isEqualTo(BikeStationStatus.OCCUPIED);
    }

    private Bike bikeOfType(UUID id, BikeType bikeType) {
        Bike bike = new Bike();
        bike.setId(id);
        bike.setBikeType(bikeType);
        return bike;
    }
}
