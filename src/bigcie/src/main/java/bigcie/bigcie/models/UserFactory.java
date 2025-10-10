package bigcie.bigcie.models;

import bigcie.bigcie.entities.Operator;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.models.enums.UserType;

public class UserFactory {

    private static final UserFactory INSTANCE = new UserFactory();

    private UserFactory() {

    }

    public static UserFactory getInstance() {
        return INSTANCE;
    }

    public User createUser(UserType userType) {
        User user = null;
        switch (userType) {
            case OPERATOR -> user = new Operator();
            case RIDER -> user = new Rider();
        }
        return user;
    }
}
