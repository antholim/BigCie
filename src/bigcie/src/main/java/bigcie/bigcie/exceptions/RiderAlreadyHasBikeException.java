package bigcie.bigcie.exceptions;

public class RiderAlreadyHasBikeException extends RuntimeException {

    public RiderAlreadyHasBikeException() {
        super("Rider already has a bike checked out");
    }

    public RiderAlreadyHasBikeException(String message) {
        super(message);
    }
}

