package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;

import java.util.List;
import java.util.UUID;

public class BronzeTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.BRONZE;


    @Override
    public void handleTierBenefits(UUID userId) {

    }

    @Override
    public void evaluateTierUpgrade(Rider rider, LoyaltyTierContext tierContext) {
        List<Reservation> reservations = tierContext.getReservationService().getExpiredReservationsPastYearByUserId(rider.getId());

    }

    private boolean eligibleForSilverTier() {
        return totalPoints >= 1000;
    }

    private boolean eligibleForGoldTier() {

    }
}
