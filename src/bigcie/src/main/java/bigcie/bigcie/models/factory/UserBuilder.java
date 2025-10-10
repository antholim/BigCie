package bigcie.bigcie.models.factory;

import bigcie.bigcie.models.User;

public interface UserBuilder<T extends User> {
    UserBuilder<T> id(java.util.UUID id);
    UserBuilder<T> username(String username);
    UserBuilder<T> email(String email);
    UserBuilder<T> password(String password);
    T build();
}

