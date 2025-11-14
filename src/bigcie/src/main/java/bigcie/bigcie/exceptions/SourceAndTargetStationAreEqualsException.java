package bigcie.bigcie.exceptions;

public class SourceAndTargetStationAreEqualsException extends RuntimeException {

    public SourceAndTargetStationAreEqualsException() {
        super("Source and target station cannot be the same");
    }

    public SourceAndTargetStationAreEqualsException(String message) {
        super(message);
    }
}
