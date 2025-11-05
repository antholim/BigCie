package bigcie.bigcie.constants.prices;

import bigcie.bigcie.entities.enums.PricingPlan;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PricingConfig {
    private Map<PricingPlan, Double> pricingPlanRates;
    private PricingConfig() {
        this.pricingPlanRates = Map.of(
                PricingPlan.SINGLE_RIDE, 0.0,
                PricingPlan.DAY_PASS, Prices.DAY_PASS_PRICE,
                PricingPlan.MONTHLY_PASS, Prices.MONTHLY_PASS_PRICE);
    }
    public double getPriceForPlan(PricingPlan pricingPlan) {
        return this.pricingPlanRates.get(pricingPlan);
    }
}
