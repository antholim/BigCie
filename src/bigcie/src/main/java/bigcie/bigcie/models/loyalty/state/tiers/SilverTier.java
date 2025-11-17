package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;

import java.util.UUID;

public class SilverTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.SILVER;

    @Override
    public void handleTierBenefits(UUID userId) {

    }

    private boolean eligibleForGoldTier(int totalPoints) {

    }

}

