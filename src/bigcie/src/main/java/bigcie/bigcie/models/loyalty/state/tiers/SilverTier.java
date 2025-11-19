package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Getter
@Component
public class SilverTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.SILVER;

    @Override
    public void handleTierBenefits(UUID userId) {

    }

    @Override
    public void evaluateTierUpgrade(Rider rider, LoyaltyTierContext ctx) {
        if (evaluateToDefault(rider, ctx)) {
            ctx.getNotificationService().notifyUserLoyaltyStatusChange(rider.getId(), LoyaltyTier.DEFAULT);
            return;
        }
        if (ctx.getTripService().meetsWeeklyTripRequirement(rider.getId(), 5, 3)) {
            rider.setLoyaltyTier(LoyaltyTier.GOLD);
            ctx.getUserService().updateUser(rider);
            ctx.getNotificationService().notifyUserLoyaltyStatusChange(rider.getId(), LoyaltyTier.GOLD);
            return;
        }

    }

}

