package bigcie.bigcie.services;

import bigcie.bigcie.constants.prices.Prices;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.services.interfaces.IPriceService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PriceService implements IPriceService {

    @Override
    public double calculatePrice(LocalDateTime startTime, LocalDateTime endTime, BikeType bikeType,
            PricingPlan pricingPlan, int discountPercentage
    ) {
        switch (pricingPlan) {
            case DAY_PASS, MONTHLY_PASS -> {
                return 0;
            }
            default -> {
                long seconds = Duration.between(startTime, endTime).getSeconds();
                long minutes = (long) Math.ceil(seconds / 60.0);
                long units = (long) Math.ceil(minutes / 5.0);
                return Math.round(
                        ((units * Prices.PRICE_PER_5_MINUTES
                                + (bikeType == BikeType.E_BIKE ? Prices.E_BIKE_SURCHARGE : 0))
                                * (100 - discountPercentage) / 100.0)
                                * 100.0
                ) / 100.0;
            }

        }
    }

    @Override
    public double getFiveMinuteRate() {
        return Prices.PRICE_PER_5_MINUTES;
    }

    @Override
    public double getEBikeSurcharge() {
        return Prices.E_BIKE_SURCHARGE;
    }
}
