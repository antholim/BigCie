package bigcie.bigcie.models.loyalty.state;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;

import java.util.UUID;

public interface ILoyaltyTierState {
    void handleTierBenefits(UUID userId);
    void evaluateTierUpgrade(Rider rider, LoyaltyTierContext tierContext);
}
