package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.PricingPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("plan_bills")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlanBill extends  Bill {
    private PricingPlan pricingPlan;
}
