package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;
import java.util.UUID;

@TypeAlias("trip")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trip extends Bill {

    private UUID bikeId;
    private UUID bikeStationStartId;
    private UUID bikeStationEndId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TripStatus status;
    private double distanceInKm;
    private BikeType bikeType;
    private PricingPlan pricingPlan;
    private int discountApplied = 0; // Discount percentage applied (loyalty tier + operator discount)

    public static class Builder {
        private final Trip trip = new Trip();

        public Builder id(UUID id) {
            trip.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            trip.userId = userId;
            return this;
        }

        public Builder bikeId(UUID bikeId) {
            trip.bikeId = bikeId;
            return this;
        }

        public Builder bikeStationStartId(UUID id) {
            trip.bikeStationStartId = id;
            return this;
        }

        public Builder bikeStationEndId(UUID id) {
            trip.bikeStationEndId = id;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            trip.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            trip.endDate = endDate;
            return this;
        }

        public Builder status(TripStatus status) {
            trip.status = status;
            return this;
        }

        public Builder distanceInKm(double distanceInKm) {
            trip.distanceInKm = distanceInKm;
            return this;
        }

        public Builder cost(double cost) {
            trip.cost = cost;
            return this;
        }

        public Builder bikeType(BikeType bikeType) {
            trip.bikeType = bikeType;
            return this;
        }

        public Builder pricingPlan(PricingPlan pricingPlan) {
            trip.pricingPlan = pricingPlan;
            return this;
        }

        public Builder discountApplied(int discountApplied) {
            trip.discountApplied = discountApplied;
            return this;
        }

        public Builder paymentInfoId(UUID paymentInfoId) {
            trip.paymentInfoId = paymentInfoId;
            return this;
        }

        public Trip build() {
            // Optionally: auto-generate ID if null
            if (trip.id == null) {
                trip.id = UUID.randomUUID();
            }
            return trip;
        }
    }
}