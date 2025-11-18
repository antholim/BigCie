package bigcie.bigcie.models.loyalty.state;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;

import java.util.List;
import java.util.UUID;

public interface ILoyaltyTierState {
    void handleTierBenefits(UUID userId);
    void evaluateTierUpgrade(Rider rider, LoyaltyTierContext tierContext);

    default boolean evaluateToDefault(Rider rider, LoyaltyTierContext ctx) {
        List<Reservation> expiredReservationsPastYear = ctx.getReservationLookup().getExpiredReservationsPastYearByUserId(rider.getId());
        if (!expiredReservationsPastYear.isEmpty()) {
            rider.setLoyaltyTier(LoyaltyTier.DEFAULT);
            ctx.getUserService().updateUser(rider);
            return true;
        }
        return false;
    }
}
