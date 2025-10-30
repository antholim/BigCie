package bigcie.bigcie.services;

import bigcie.bigcie.dtos.BikeRequest.BikeRequest;
import bigcie.bigcie.dtos.events.TripEventDto;
import bigcie.bigcie.entities.Bike;
import bigcie.bigcie.entities.BikeStation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.BikeStatus;
import bigcie.bigcie.repositories.BikeRepository;
import bigcie.bigcie.services.interfaces.IBikeService;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BikeService implements IBikeService {
    private final BikeRepository bikeRepository;
    private final BikeStationService bikeStationService;
    private final INotificationService notificationService;
    private final IUserService userService;

    public BikeService(
            BikeRepository bikeRepository,
            BikeStationService bikeStationService,
            INotificationService notificationService, IUserService userService) {
        this.bikeStationService = bikeStationService;
        this.bikeRepository = bikeRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // java
    @Override
    public Bike createBike(BikeRequest bike) {
        BikeStation station = bikeStationService.getStationById(bike.getBikeStationId());
        UUID bikeId = UUID.randomUUID();

        Bike bikeEntity = new Bike();
        bikeEntity.setId(bikeId);
        bikeEntity.setBikeType(bike.getBikeType());
        bikeEntity.setStatus(bike.getStatus());
        bikeEntity.setReservationExpiry(bike.getReservationExpiry());

        Bike savedBike = bikeRepository.save(bikeEntity);

        // build a deduplicated list of ids including the newly created bike
        List<UUID> bikesIds = station.getBikesIds();
        if (!bikesIds.contains(bikeId)) {
            bikesIds.add(bikeId);
        }

        // load all bikes for those ids from repository so the count reflects repository state (handles non-zero existing counts)
        List<Bike> bikesIterable = bikeRepository.findAllById(bikesIds);
        int numberOfBikesDocked = 0;
        for (Bike b : bikesIterable) {
            if (b.getStatus() == BikeStatus.AVAILABLE ||
                    b.getStatus() == BikeStatus.MAINTENANCE ||
                    b.getStatus() == BikeStatus.RESERVED) {
                numberOfBikesDocked++;
            }
        }

        station.setBikesIds(bikesIds);
        station.setNumberOfBikesDocked(numberOfBikesDocked);
        bikeStationService.updateStation(station.getId(), station);

        return savedBike;
    }


    @Override
    public Bike getBikeById(UUID id) {
        return bikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bike not found with id: " + id));
    }

    @Override
    public List<Bike> getAllBikes() {
        return bikeRepository.findAll();
    }

    @Override
    public List<Bike> getBikesByStatus(BikeStatus status) {
        return bikeRepository.findByStatus(status);
    }

    @Override
    public Bike updateBike(UUID id, Bike bike) {
        Bike existingBike = getBikeById(id);
        BikeStatus previousStatus = existingBike.getStatus();
        notificationService.notifyBikeStatusChange(id, bike.getStatus());
        existingBike.setStatus(bike.getStatus());
        existingBike.setBikeType(bike.getBikeType());
        existingBike.setReservationExpiry(bike.getReservationExpiry());
        Bike updatedBike = bikeRepository.save(existingBike);
        emitTripEventIfNeeded(updatedBike, previousStatus);
        return updatedBike;
    }

    @Override
    public void deleteBike(UUID id) {
        bikeRepository.deleteById(id);
    }

    @Override
    public Bike updateBikeStatus(UUID id, BikeStatus status) {
        Bike bike = getBikeById(id);
        BikeStatus previousStatus = bike.getStatus();
        notificationService.notifyBikeStatusChange(id, status);
        bike.setStatus(status);
        Bike updatedBike = bikeRepository.save(bike);
        emitTripEventIfNeeded(updatedBike, previousStatus);
        return updatedBike;
    }

    @Override
    public List<Bike> bulkCreateBikes(List<BikeRequest> bikes) {
        return bikes.stream()
                .map(this::createBike)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getBikeIdFromRiderId(UUID riderId) {
        User user = userService.getUserByUUID(riderId);
        if (user instanceof Rider rider) {
            rider = (Rider) user;
            return rider.getCurrentBikes();
        }
        return List.of();
    }

    private void emitTripEventIfNeeded(Bike bike, BikeStatus previousStatus) {
        BikeStatus currentStatus = bike.getStatus();
        if (currentStatus == previousStatus) {
            return;
        }

        TripEventDto.TripEventType eventType = null;
        if (currentStatus == BikeStatus.ON_TRIP && previousStatus != BikeStatus.ON_TRIP) {
            eventType = TripEventDto.TripEventType.TRIP_STARTED;
        } else if (previousStatus == BikeStatus.ON_TRIP && currentStatus != BikeStatus.ON_TRIP) {
            eventType = TripEventDto.TripEventType.TRIP_ENDED;
        }

        if (eventType != null) {
            TripEventDto event = new TripEventDto(
                    bike.getId(),
                    null,
                    null,
                    eventType,
                    OffsetDateTime.now());
            notificationService.publishTripEvent(event);
        }
    }
}
