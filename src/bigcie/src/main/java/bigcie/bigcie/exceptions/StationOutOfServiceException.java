package bigcie.bigcie.exceptions;

public class StationOutOfServiceException extends RuntimeException {

    public StationOutOfServiceException() {
        super("Station is out of service");
    }

    public StationOutOfServiceException(String message) {
        super(message);
    }
}
