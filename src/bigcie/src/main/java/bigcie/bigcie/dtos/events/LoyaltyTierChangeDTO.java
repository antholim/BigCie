package bigcie.bigcie.dtos.events;

import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class LoyaltyTierChangeDTO {
    private UUID userId;
    private LoyaltyTier loyaltyTier;
}
