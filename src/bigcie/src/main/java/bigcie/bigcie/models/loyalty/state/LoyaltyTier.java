package bigcie.bigcie.models.loyalty.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoyaltyTier {
    DEFAULT(0,0),
    BRONZE(5,0),
    SILVER(10,2),
    GOLD(15,5);

    private final int discountPercentage;
    private final int extraReservationMinutesHold;
}
