package bigcie.bigcie.models.loyalty;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.models.loyalty.state.ILoyaltyTierState;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import bigcie.bigcie.models.loyalty.state.LoyaltyTierFactory;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.IReservationService;
import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IUserService;
import bigcie.bigcie.services.read.interfaces.IReservationLookup;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Getter
public class LoyaltyTierContext {
    private final IUserService userService;
    private final IReservationLookup reservationLookup;
    private final ITripService tripService;
    private final LoyaltyTierFactory loyaltyTierFactory;
    private final INotificationService notificationService;

    public LoyaltyTierContext(IUserService userService, IReservationLookup reservationLookup, ITripService tripService, LoyaltyTierFactory loyaltyTierFactory, INotificationService notificationService) {
        this.userService = userService;
        this.reservationLookup = reservationLookup;
        this.tripService = tripService;
        this.loyaltyTierFactory = loyaltyTierFactory;
        this.notificationService = notificationService;
    }

    public void evaluateUserTierUpgrade(Rider rider) {
        ILoyaltyTierState tierState = loyaltyTierFactory.getLoyaltyTierState(rider.getLoyaltyTier());
        tierState.evaluateTierUpgrade(rider, this);
    }

    public void evaluateUserTierUpgrade(UUID riderId) {
        Rider rider = (Rider) userService.getUserByUUID(riderId);
        ILoyaltyTierState tierState = loyaltyTierFactory.getLoyaltyTierState(rider.getLoyaltyTier());
        tierState.evaluateTierUpgrade(rider, this);
    }
}
