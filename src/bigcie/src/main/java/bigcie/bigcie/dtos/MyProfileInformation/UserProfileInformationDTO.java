package bigcie.bigcie.dtos.MyProfileInformation;

import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileInformationDTO {
    private LoyaltyTier loyaltyTier;
}
