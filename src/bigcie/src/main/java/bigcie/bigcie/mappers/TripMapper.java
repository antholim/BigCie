package bigcie.bigcie.mappers;

import bigcie.bigcie.dtos.TripInfo.TripDto;
import bigcie.bigcie.entities.Trip;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripMapper {
    public List<TripDto> toTripDtoList(List<bigcie.bigcie.entities.Trip> trips) {
        return trips.stream().map(this::toTripDto).toList();
    }

    public TripDto toTripDto(Trip trip) {
        TripDto dto = new TripDto.Builder()
                .id(trip.getId())
                .userId(trip.getUserId())
                .bikeId(trip.getBikeId())
                // .bikeStationStart(trip.getBikeStationStartId())
                // .bikeStationEnd(trip.getBikeStationEndId())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .distanceInKm(trip.getDistanceInKm())
                .pricingPlan(trip.getPricingPlan())
                .bikeType(trip.getBikeType())
                .cost(trip.getCost())
                .build();
        return dto;
    }

}
