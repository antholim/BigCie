package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.entities.factory.UserBuilder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Operator extends User  {
    public Operator() {
        this.type = UserType.OPERATOR;
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

    public static class Builder implements UserBuilder<Operator> {
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
        public Operator build() {
            Operator operator = new Operator();
            operator.setId(this.id);
            operator.setUsername(this.username);
            operator.setEmail(this.email);
            operator.setPassword(this.password);
            operator.type = UserType.OPERATOR;
            return operator;
        }
    }
}
