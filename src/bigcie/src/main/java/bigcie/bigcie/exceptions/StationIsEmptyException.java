package bigcie.bigcie.exceptions;

public class StationIsEmptyException extends RuntimeException{
    public  StationIsEmptyException() {
        super("The station is empty");
    }

    public StationIsEmptyException(String message) {
        super(message);
    }
}
