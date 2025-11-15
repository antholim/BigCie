package bigcie.bigcie.models.loyalty.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoyaltyTier {
    DEFAULT(0),
    BRONZE(5),
    SILVER(10),
    GOLD(15);

    private final int discountPercentage;
}
