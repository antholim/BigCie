package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;

import java.util.UUID;

public class GoldTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.SILVER;

    @Override
    public void handleTierBenefits(UUID userId) {

    }

    @Override
    public void evaluateTierUpgrade(Rider rider, LoyaltyTierContext tierContext) {

    }
}

