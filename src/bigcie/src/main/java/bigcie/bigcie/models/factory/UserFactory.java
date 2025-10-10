// ...existing code...
import bigcie.bigcie.models.factory.UserBuilder;
// ...existing code...
    public static UserBuilder<?> getBuilder(UserType type) {
        return switch (type) {
            case OPERATOR -> new Operator.Builder();
            case RIDER -> new Rider.Builder();
        };
    }
// ...existing code...

