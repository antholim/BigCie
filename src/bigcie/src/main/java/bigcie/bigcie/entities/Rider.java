package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.entities.factory.UserBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@TypeAlias("rider")
public class Rider extends User {
    public Rider() {
        this.type = UserType.RIDER;
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
