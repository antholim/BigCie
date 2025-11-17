package bigcie.bigcie.models.loyalty.state;

import bigcie.bigcie.models.loyalty.state.tiers.BronzeTier;
import bigcie.bigcie.models.loyalty.state.tiers.DefaultTier;
import bigcie.bigcie.models.loyalty.state.tiers.GoldTier;
import bigcie.bigcie.models.loyalty.state.tiers.SilverTier;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyTierFactory {
    private final DefaultTier defaultTier;
    private final BronzeTier bronzeTier;
    private final SilverTier silverTier;
    private final GoldTier goldTier;

    public LoyaltyTierFactory(DefaultTier defaultTier, BronzeTier bronzeTier, SilverTier silverTier, GoldTier goldTier) {
        this.defaultTier = defaultTier;
        this.bronzeTier = bronzeTier;
        this.silverTier = silverTier;
        this.goldTier = goldTier;
    }

    public ILoyaltyTierState getLoyaltyTierState(LoyaltyTier tier) {
        return switch (tier) {
            case DEFAULT -> defaultTier;
            case BRONZE -> bronzeTier;
            case SILVER -> silverTier;
            case GOLD -> goldTier;
        };
    }
}
