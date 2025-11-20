package bigcie.bigcie.services.read;

import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.repositories.BikeStationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BikeStationLookup.class)
class BikeStationLookupTest {

    @MockBean
    private BikeStationRepository bikeStationRepository;

    @Autowired
    private BikeStationLookup bikeStationLookup;

    @Test
    void returnsUnknownWhenIdIsNull() {
        assertThat(bikeStationLookup.getStationNameById(null)).isEqualTo("Unknown Station");
    }

    @Test
    void retrievesStationFromRepository() {
        UUID stationId = UUID.randomUUID();
        BikeStation station = new BikeStation();
        station.setId(stationId);
        station.setName("Campus");
        when(bikeStationRepository.findById(stationId)).thenReturn(Optional.of(station));

        assertThat(bikeStationLookup.getStationNameById(stationId)).isEqualTo("Campus");
    }
}
