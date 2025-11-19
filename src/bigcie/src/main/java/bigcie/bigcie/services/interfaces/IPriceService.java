package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;

import java.time.LocalDateTime;

public interface IPriceService {
    double calculatePrice(LocalDateTime startTime, LocalDateTime endTime, BikeType bikeType, PricingPlan pricingPlan, int discountPercentage);

    double getFiveMinuteRate();

    double getEBikeSurcharge();
}
