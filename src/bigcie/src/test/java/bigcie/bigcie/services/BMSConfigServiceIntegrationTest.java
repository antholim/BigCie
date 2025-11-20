package bigcie.bigcie.services;

import bigcie.bigcie.entities.BMSConfig;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.repositories.BikeStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BMSConfigService.class)
class BMSConfigServiceIntegrationTest {

    @MockBean
    private BikeRepository bikeRepository;

    @MockBean
    private BikeStationRepository bikeStationRepository;

    @Autowired
    private BMSConfigService bmsConfigService;

    @TempDir
    Path tempDir;

    private Path configPath;

    @BeforeEach
    void setUp() throws IOException {
        String json = """
                {
                  "bikeStations": [
                    {
                      "id": "11111111-1111-1111-1111-111111111111",
                      "name": "Central",
                      "status": "EMPTY",
                      "latitude": 45.0,
                      "longitude": -73.0,
                  "address": "123 Main",
                  "capacity": 10,
                  "standardBikesDocked": 0,
                  "ebikesDocked": 0,
                  "bikesIds": [],
                  "reservationHoldTimeMinutes": 15
                }
                  ],
                  "bikes": [
                    {
                      "id": "22222222-2222-2222-2222-222222222222",
                      "status": "AVAILABLE",
                      "bikeType": "STANDARD",
                      "reservationExpiry": "2030-01-01T00:00:00"
                    }
                  ]
                }
                """;
        configPath = tempDir.resolve("bmsConfig.json");
        Files.writeString(configPath, json);
    }

    @Test
    void createConfigLoadsBikesAndStationsAndPersists() {
        BMSConfig config = bmsConfigService.createConfig(configPath.toString());

        assertThat(config).isNotNull();
        assertThat(config.getBikeStations()).hasSize(1);
        assertThat(config.getBikes()).hasSize(1);

        bmsConfigService.populateDB();

        verify(bikeRepository).saveAll(config.getBikes());
        verify(bikeStationRepository).saveAll(config.getBikeStations());
        assertThat(bmsConfigService.getBMSConfig()).isSameAs(config);
    }
}
