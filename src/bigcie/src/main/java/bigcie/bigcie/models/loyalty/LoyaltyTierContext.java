package bigcie.bigcie.models.loyalty;

import bigcie.bigcie.models.loyalty.state.LoyaltyTierFactory;
import bigcie.bigcie.services.interfaces.INotificationService;
import bigcie.bigcie.services.interfaces.IReservationService;
import bigcie.bigcie.services.interfaces.ITripService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class LoyaltyTierContext {
    private final IUserService userService;
    private final IReservationService reservationService;
    private final ITripService tripService;
    private final LoyaltyTierFactory loyaltyTierFactory;
    private final INotificationService notificationService;

    public LoyaltyTierContext(IUserService userService, IReservationService reservationService, ITripService tripService, LoyaltyTierFactory loyaltyTierFactory, INotificationService notificationService) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.tripService = tripService;
        this.loyaltyTierFactory = loyaltyTierFactory;
        this.notificationService = notificationService;
    }
}
