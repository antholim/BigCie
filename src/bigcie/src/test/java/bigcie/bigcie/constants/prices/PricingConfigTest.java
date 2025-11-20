package bigcie.bigcie.constants.prices;

import bigcie.bigcie.entities.enums.PricingPlan;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class PricingConfigTest {

    @Test
    void returnsConfiguredPlanPrices() {
        PricingConfig pricingConfig = instantiatePricingConfig();

        assertThat(pricingConfig.getPriceForPlan(PricingPlan.DAY_PASS)).isEqualTo(Prices.DAY_PASS_PRICE);
        assertThat(pricingConfig.getPriceForPlan(PricingPlan.MONTHLY_PASS)).isEqualTo(Prices.MONTHLY_PASS_PRICE);
        assertThat(pricingConfig.getPriceForPlan(PricingPlan.SINGLE_RIDE)).isZero();
    }

    private PricingConfig instantiatePricingConfig() {
        try {
            Constructor<PricingConfig> ctor = PricingConfig.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate PricingConfig", e);
        }
    }
}
