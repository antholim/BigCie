package bigcie.bigcie.dtos.TripInfo;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripDto {
    private UUID id;
    private UUID userId;
    private UUID bikeId;
    private String bikeStationStart;
    private String bikeStationEnd;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TripStatus status;
    private double distanceInKm;
    private double cost;
    private BikeType bikeType;
    private PricingPlan pricingPlan;
    private PaymentInfoDto paymentInfo;
    private double flexDollarsUsed;
    private double amountCharged;

    public static class Builder {
        private final TripDto tripDto = new TripDto();

        public Builder id(UUID id) {
            tripDto.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            tripDto.userId = userId;
            return this;
        }

        public Builder bikeId(UUID bikeId) {
            tripDto.bikeId = bikeId;
            return this;
        }

        public Builder bikeStationStart(String name) {
            tripDto.bikeStationStart = name;
            return this;
        }

        public Builder bikeStationEnd(String name) {
            tripDto.bikeStationEnd = name;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            tripDto.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            tripDto.endDate = endDate;
            return this;
        }

        public Builder status(TripStatus status) {
            tripDto.status = status;
            return this;
        }

        public Builder distanceInKm(double distanceInKm) {
            tripDto.distanceInKm = distanceInKm;
            return this;
        }

        public Builder cost(double cost) {
            tripDto.cost = cost;
            return this;
        }

        public Builder bikeType(BikeType bikeType) {
            tripDto.bikeType = bikeType;
            return this;
        }

        public Builder pricingPlan(PricingPlan pricingPlan) {
            tripDto.pricingPlan = pricingPlan;
            return this;
        }

        public Builder paymentInfo(PaymentInfoDto paymentInfo) {
            tripDto.paymentInfo = paymentInfo;
            return this;
        }

        public Builder flexDollarsUsed(double flexDollarsUsed) {
            tripDto.flexDollarsUsed = flexDollarsUsed;
            return this;
        }

        public Builder amountCharged(double amountCharged) {
            tripDto.amountCharged = amountCharged;
            return this;
        }

        public TripDto build() {
            if (tripDto.id == null) {
                tripDto.id = UUID.randomUUID();
            }
            return tripDto;
        }
    }
}
