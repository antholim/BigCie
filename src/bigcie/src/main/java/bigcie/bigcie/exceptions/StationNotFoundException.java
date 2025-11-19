package bigcie.bigcie.exceptions;

public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException() {
        super("Station not found");
    }

    public StationNotFoundException(String message) {
        super(message);
    }
}
