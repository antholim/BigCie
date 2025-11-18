package bigcie.bigcie.exceptions;

public class UserIsNotRiderException extends RuntimeException {

    public UserIsNotRiderException() {
        super("User is not a rider");
    }

    public UserIsNotRiderException(String message) {
        super(message);
    }
}
