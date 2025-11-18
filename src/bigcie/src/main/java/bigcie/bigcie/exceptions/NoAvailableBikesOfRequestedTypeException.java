package bigcie.bigcie.exceptions;

public class NoAvailableBikesOfRequestedTypeException extends RuntimeException {

    public NoAvailableBikesOfRequestedTypeException() {
        super("No available bikes of the requested type found");
    }

    public NoAvailableBikesOfRequestedTypeException(String message) {
        super(message);
    }
}
