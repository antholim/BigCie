package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BikeService.class)
class BikeServiceIntegrationTest {

    @MockBean
    private BikeRepository bikeRepository;

    @MockBean
    private BikeStationService bikeStationService;

    @MockBean
    private INotificationService notificationService;

    @MockBean
    private IUserService userService;

    @Autowired
    private BikeService bikeService;

    @Test
    void createBikeUpdatesStationCountsAndIds() {
        UUID stationId = UUID.randomUUID();
        UUID existingBikeId = UUID.randomUUID();
        UUID newBikeId = UUID.randomUUID();

        BikeStation station = new BikeStation();
        station.setId(stationId);
        station.setCapacity(5);
        station.setBikesIds(new ArrayList<>(List.of(existingBikeId)));

        Bike existingBike = new Bike();
        existingBike.setId(existingBikeId);
        existingBike.setBikeType(BikeType.STANDARD);
        existingBike.setStatus(BikeStatus.AVAILABLE);

        Bike savedBike = new Bike();
        savedBike.setId(newBikeId);
        savedBike.setBikeType(BikeType.E_BIKE);
        savedBike.setStatus(BikeStatus.RESERVED);
        savedBike.setReservationExpiry(LocalDateTime.now().plusHours(1));

        when(bikeStationService.getStationById(stationId)).thenReturn(station);
        when(bikeRepository.save(any(Bike.class))).thenAnswer((Answer<Bike>) invocation -> invocation.getArgument(0));
        when(bikeRepository.findAllById(any())).thenAnswer((Answer<List<Bike>>) invocation -> {
            List<UUID> ids = invocation.getArgument(0);
            // return bikes matching requested ids
            return ids.stream().map(id -> {
                if (id.equals(existingBikeId)) {
                    return existingBike;
                }
                Bike copy = new Bike();
                copy.setId(id);
                copy.setBikeType(BikeType.E_BIKE);
                copy.setStatus(BikeStatus.RESERVED);
                copy.setReservationExpiry(savedBike.getReservationExpiry());
                return copy;
            }).toList();
        });
        when(bikeStationService.updateStation(eq(stationId), any(BikeStation.class)))
                .thenAnswer((Answer<BikeStation>) invocation -> invocation.getArgument(1));

        BikeRequest request = new BikeRequest();
        request.setBikeStationId(stationId);
        request.setBikeType(BikeType.E_BIKE);
        request.setStatus(BikeStatus.RESERVED);
        request.setReservationExpiry(savedBike.getReservationExpiry());

        Bike result = bikeService.createBike(request);

        UUID createdId = result.getId();
        assertThat(createdId).isNotNull();
        ArgumentCaptor<BikeStation> stationCaptor = ArgumentCaptor.forClass(BikeStation.class);
        verify(bikeStationService).updateStation(eq(stationId), stationCaptor.capture());
        BikeStation updatedStation = stationCaptor.getValue();
        assertThat(updatedStation.getBikesIds()).contains(existingBikeId, createdId);
        assertThat(updatedStation.getStandardBikesDocked()).isEqualTo(1);
        assertThat(updatedStation.getEBikesDocked()).isEqualTo(1);
    }

    @Test
    void updateBikeStatusEmitsTripEvents() {
        UUID bikeId = UUID.randomUUID();
        Bike availableBike = new Bike();
        availableBike.setId(bikeId);
        availableBike.setStatus(BikeStatus.AVAILABLE);
        availableBike.setBikeType(BikeType.STANDARD);

        Bike onTripBike = new Bike();
        onTripBike.setId(bikeId);
        onTripBike.setStatus(BikeStatus.ON_TRIP);
        onTripBike.setBikeType(BikeType.STANDARD);

        when(bikeRepository.findById(bikeId))
                .thenReturn(Optional.of(availableBike))
                .thenReturn(Optional.of(onTripBike));
        when(bikeRepository.save(any(Bike.class))).thenAnswer((Answer<Bike>) invocation -> invocation.getArgument(0));

        bikeService.updateBikeStatus(bikeId, BikeStatus.ON_TRIP);
        bikeService.updateBikeStatus(bikeId, BikeStatus.AVAILABLE);

        verify(notificationService, times(2)).notifyBikeStatusChange(eq(bikeId), any());
        ArgumentCaptor<TripEventDto> tripEventCaptor = ArgumentCaptor.forClass(TripEventDto.class);
        verify(notificationService, times(2)).publishTripEvent(tripEventCaptor.capture());

        List<TripEventDto.TripEventType> eventTypes = tripEventCaptor.getAllValues()
                .stream()
                .map(TripEventDto::type)
                .toList();
        assertThat(eventTypes).containsExactly(
                TripEventDto.TripEventType.TRIP_STARTED,
                TripEventDto.TripEventType.TRIP_ENDED);
    }
}
