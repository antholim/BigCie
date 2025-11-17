package bigcie.bigcie.models.loyalty;

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

    public LoyaltyTierContext(IUserService userService, IReservationService reservationService, ITripService tripService) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.tripService = tripService;
    }
}
