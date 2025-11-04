package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.PricingPlan;
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

@TypeAlias("rider")
@Getter
@Setter
public class Rider extends User {
    public Rider() {
        this.type = UserType.RIDER;
    }

    private List<PaymentInfo> paymentInfos = new ArrayList<>();
    private String address;
    private List<UUID> currentBikes = new ArrayList<>();
    private List<UUID> activeTripId = new ArrayList<>();
    private PricingPlan pricingPlan = PricingPlan.SINGLE_RIDE;

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

    public static class Builder implements UserBuilder<Rider> {
        private UUID id;
        private String username;
        private String email;
        private String password;

        @Override
        public Rider.Builder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public Rider.Builder username(String username) {
            this.username = username;
            return this;
        }

        @Override
        public Rider.Builder email(String email) {
            this.email = email;
            return this;
        }

        @Override
        public Rider.Builder password(String password) {
            this.password = password;
            return this;
        }

        @Override
        public Rider build() {
            Rider Rider = new Rider();
            Rider.setId(this.id);
            Rider.setUsername(this.username);
            Rider.setEmail(this.email);
            Rider.setPassword(this.password);
            Rider.type = UserType.RIDER;
            return Rider;
        }
    }
}
