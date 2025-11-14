package bigcie.bigcie.exceptions;

public class StationIsFullException extends RuntimeException {
    public StationIsFullException() {
        super("The station is full");
    }

    public StationIsFullException(String message) {
        super(message);
    }
}
