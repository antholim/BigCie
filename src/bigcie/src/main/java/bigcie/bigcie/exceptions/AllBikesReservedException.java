package bigcie.bigcie.exceptions;

public class AllBikesReservedException extends RuntimeException {

    public AllBikesReservedException() {
        super("All bikes are reserved");
    }

    public AllBikesReservedException(String message) {
        super(message);
    }
}
