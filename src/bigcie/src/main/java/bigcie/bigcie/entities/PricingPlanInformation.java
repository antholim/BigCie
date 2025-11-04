package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.PricingPlan;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PricingPlanInformation {
    private PricingPlan pricingPlan = PricingPlan.SINGLE_RIDE;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
