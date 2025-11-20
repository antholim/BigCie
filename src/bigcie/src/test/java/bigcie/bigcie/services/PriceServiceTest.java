package bigcie.bigcie.services;

import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PriceServiceTest {

    private final PriceService priceService = new PriceService();

    @Test
    void calculatePriceSingleRideWithEBikeSurchargeAndDiscount() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        LocalDateTime end = LocalDateTime.now();

        double price = priceService.calculatePrice(start, end, BikeType.E_BIKE, PricingPlan.SINGLE_RIDE, 10);

        assertThat(price).isEqualTo(4.5);
    }

    @Test
    void calculatePriceReturnsZeroForPasses() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        assertThat(priceService.calculatePrice(start, end, BikeType.STANDARD, PricingPlan.DAY_PASS, 0)).isZero();
        assertThat(priceService.calculatePrice(start, end, BikeType.STANDARD, PricingPlan.MONTHLY_PASS, 0)).isZero();
    }
}
