package bigcie.bigcie.exceptions;

public class NoDefaultPaymentMethodFoundException extends RuntimeException {

    public NoDefaultPaymentMethodFoundException() {
        super("No default payment method found for rider");
    }

    public NoDefaultPaymentMethodFoundException(String message) {
        super(message);
    }
}
