package bigcie.bigcie.assemblers.facades;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.mappers.TripMapper;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class TripAssembler {
    private final TripMapper tripMapper;
    private final IBikeStationService bikeStationService;
    private Map<UUID, String> stationNameCache = new HashMap<>();
    public TripAssembler(TripMapper tripMapper, IBikeStationService bikeStationService) {
        this.tripMapper = tripMapper;
        this.bikeStationService = bikeStationService;
    }

    public List<TripDto> enrichTripDtoList(List<Trip> trips) {
        List<TripDto> tripDtos = tripMapper.toTripDtoList(trips);
        int n = Math.min(tripDtos.size(), trips.size());
        for (int i = 0; i < n; i++) {
            TripDto dto = tripDtos.get(i);

            if (dto.getBikeStationStart() != null) {
                UUID startId = trips.get(i).getBikeStationStartId();
                dto.setBikeStationStart(bikeStationService.getStationNameById(startId));
            }

            if (dto.getBikeStationEnd() != null) {
                UUID endId = trips.get(i).getBikeStationEndId();
                dto.setBikeStationEnd(bikeStationService.getStationNameById(endId));
            }
        }
        return tripDtos;
    }


    private String getStationNameWithCache(UUID stationId) {
        if (this.stationNameCache.containsKey(stationId)) {
            return this.stationNameCache.get(stationId);
        } else {
            String stationName = bikeStationService.getStationNameById(stationId);
            this.stationNameCache.put(stationId, stationName);
            return stationName;
        }
    }

//    @Scheduled(fixedRate = 1000 * 60 * 60)
//    public void clearCache() {
//        localStationNameCache.clear();
//        log.info("Clearing cache for station name");
//    }

}
