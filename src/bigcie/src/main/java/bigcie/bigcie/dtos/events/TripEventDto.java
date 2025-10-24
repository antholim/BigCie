package bigcie.bigcie.dtos.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TripEventDto(
        UUID bikeId,
        UUID riderId,
        UUID stationId,
        TripEventType type,
        OffsetDateTime occurredAt
) {
    public enum TripEventType {
        TRIP_STARTED,
        TRIP_ENDED
    }
}
