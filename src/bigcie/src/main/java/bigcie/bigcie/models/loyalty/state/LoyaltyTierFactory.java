package bigcie.bigcie.models.loyalty.state;

import bigcie.bigcie.models.loyalty.state.tiers.BronzeTier;
import bigcie.bigcie.models.loyalty.state.tiers.DefaultTier;
import bigcie.bigcie.models.loyalty.state.tiers.GoldTier;
import bigcie.bigcie.models.loyalty.state.tiers.SilverTier;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyTierFactory {
    public ILoyaltyTierState getLoyaltyTierState(LoyaltyTier tier) {
        return switch (tier) {
            case DEFAULT -> new DefaultTier();
            case BRONZE -> new BronzeTier();
            case SILVER -> new SilverTier();
            case GOLD -> new GoldTier();
        };
    }
}
