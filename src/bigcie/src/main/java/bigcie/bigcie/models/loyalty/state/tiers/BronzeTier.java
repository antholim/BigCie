package bigcie.bigcie.models.loyalty.state.tiers;

import bigcie.bigcie.entities.Reservation;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.enums.ReservationStatus;
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
        List<Reservation> reservations = tierContext.getReservationService().getReservationsPastYearByUserIdAndStatus(rider.getId(), ReservationStatus.COMPLETED);
        if (reservations.size() >= 5 && tierContext.getTripService().meetsMonthlyTripRequirement(rider.getId(), 5, 3)) {
            rider.setLoyaltyTier(LoyaltyTier.SILVER);
        }


    }
}
