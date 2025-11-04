package bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest;

import bigcie.bigcie.entities.enums.PricingPlan;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPlanRequest {
    private PricingPlan pricingPlan;
}
