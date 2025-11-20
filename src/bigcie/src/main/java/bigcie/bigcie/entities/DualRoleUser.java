package bigcie.bigcie.entities;

import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.entities.factory.UserBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * DualRoleUser represents a user who can act as both an Operator and a Rider.
 * This allows operational staff to test the system as riders, with special
 * discounts applied.
 */
@TypeAlias("dualRoleUser")
@Getter
@Setter
public class DualRoleUser extends User {
    public DualRoleUser() {
        this.type = UserType.DUAL_ROLE;
    }

    // Operator-related fields
    private boolean operatorEnabled = true;

    // Rider-related fields
    private List<PaymentInfo> paymentInfos = new ArrayList<>();
    private String address;
    private List<UUID> currentBikes = new ArrayList<>();
    private List<UUID> activeTripId = new ArrayList<>();
    private PricingPlanInformation pricingPlanInformation = new PricingPlanInformation();
    private LoyaltyTier loyaltyTier = LoyaltyTier.DEFAULT;
    private double flexDollars = 0.0;

    // Discount for operators acting as riders (in percentage)
    private int operatorRiderDiscount = 20; // 20% discount by default

    public PaymentInfo getDefaultPaymentInfo() {
        return paymentInfos.stream()
                .filter(PaymentInfo::isDefault)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    public static class Builder implements UserBuilder<DualRoleUser> {
        private UUID id;
        private String username;
        private String email;
        private String password;

        @Override
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        @Override
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        @Override
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        @Override
        public DualRoleUser build() {
            DualRoleUser user = new DualRoleUser();
            user.setId(this.id);
            user.setUsername(this.username);
            user.setEmail(this.email);
            user.setPassword(this.password);
            user.type = UserType.DUAL_ROLE;
            return user;
        }
    }
}
