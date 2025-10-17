package bigcie.bigcie.entities.factory;

import bigcie.bigcie.entities.User;

public interface UserBuilder<T extends User> {
    UserBuilder<T> id(java.util.UUID id);

    UserBuilder<T> username(String username);

    UserBuilder<T> email(String email);

    UserBuilder<T> password(String password);

    T build();
}
