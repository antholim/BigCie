package bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest;

import bigcie.bigcie.entities.enums.PricingPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentPlanDto {
    private PricingPlan pricingPlan;
}
