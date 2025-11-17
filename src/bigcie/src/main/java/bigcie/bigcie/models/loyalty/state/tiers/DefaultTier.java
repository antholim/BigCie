package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.Trip;
import bigcie.bigcie.models.loyalty.LoyaltyTierContext;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import lombok.Getter;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.UUID;

@Getter
@Component
public class DefaultTier implements ILoyaltyTierState {
    private final LoyaltyTier tier = LoyaltyTier.DEFAULT;

    @Override
    public void handleTierBenefits(UUID userId) {

    }

    @Override
    public void evaluateTierUpgrade(Rider rider, LoyaltyTierContext ctx) {
        List<Reservation> expiredReservationsPastYear = ctx.getReservationService().getExpiredReservationsPastYearByUserId(rider.getId());
        List<Trip> completedTripsPastYear = ctx.getTripService().getCompletedTripsPastYearByUserId(rider.getId());
        if (expiredReservationsPastYear.isEmpty() && completedTripsPastYear.size() >= 10) {
            rider.setLoyaltyTier(LoyaltyTier.BRONZE);
            ctx.getUserService().updateUser(rider);
            ctx.getNotificationService().notifyUserLoyaltyStatusChange(rider.getId(), LoyaltyTier.BRONZE);
        }
    }
}
