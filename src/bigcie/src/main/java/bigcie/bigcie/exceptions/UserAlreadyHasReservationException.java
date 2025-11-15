package bigcie.bigcie.exceptions;

public class UserAlreadyHasReservationException extends RuntimeException {

    public UserAlreadyHasReservationException() {
        super("User already has a reservation at station ");
    }

    public UserAlreadyHasReservationException(String message) {
        super(message);
    }
}
