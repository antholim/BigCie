package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class GoldTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.GOLD;

    @Override
    public void handleTierBenefits(UUID userId) {

    }

    @Override
    public void evaluateTierUpgrade(Rider rider, LoyaltyTierContext ctx) {
        if (evaluateToDefault(rider, ctx)) {
            return;
        }
        if (!ctx.getTripService().meetsWeeklyTripRequirement(rider.getId(), 5, 3)) {
            rider.setLoyaltyTier(LoyaltyTier.SILVER);
            ctx.getUserService().updateUser(rider);
            return;
        }
    }

}

