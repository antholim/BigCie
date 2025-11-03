package bigcie.bigcie.services;

import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.ITripService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TripService implements ITripService {
    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public Trip getTripById(UUID id) {
        return tripRepository.findById(id).orElse(null);
    }

    @Override
    public void createTrip(
            UUID userId,
            UUID bikeId,
            UUID bikeStationStartId,
            UUID bikeStationEndId,
            double distanceInKm,
            double cost
    ) {
        Trip trip = new Trip.Builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .bikeId(bikeId)
                .bikeStationStartId(bikeStationStartId)
                .bikeStationEndId(bikeStationEndId)
                .startDate(LocalDateTime.now())
                .status(TripStatus.ONGOING)
                .distanceInKm(distanceInKm)
                .cost(cost)
                .build();

        tripRepository.save(trip);
    }

}
