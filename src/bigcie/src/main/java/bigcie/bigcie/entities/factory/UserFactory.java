package bigcie.bigcie.entities.factory;

import bigcie.bigcie.entities.DualRoleUser;
import bigcie.bigcie.entities.Operator;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.UserType;

public class UserFactory {

    private static final UserFactory INSTANCE = new UserFactory();

    private UserFactory() {
    }

    public static UserFactory getInstance() {
        return INSTANCE;
    }

    public User createUser(UserType userType) {
        return getUserBuilder(userType).build();
    }

    public UserBuilder<?> getUserBuilder(UserType type) {
        switch (type) {
            case RIDER:
                return new Rider.Builder();
            case OPERATOR:
                return new Operator.Builder();
            case DUAL_ROLE:
                return new DualRoleUser.Builder();
            default:
                throw new IllegalArgumentException("Invalid user type: " + type);
        }
    }
}
