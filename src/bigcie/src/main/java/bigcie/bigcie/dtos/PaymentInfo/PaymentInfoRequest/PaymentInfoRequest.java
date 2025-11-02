package bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest;

import bigcie.bigcie.entities.enums.CreditCardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentInfoRequest {
    private String creditCardNumber; // Encrypted or tokenized card number
    private String cardExpiry; // MM/YY
    private String cardHolderName;
    private CreditCardType cardType; // e.g., "VISA"
    private String cvv; // Card Verification Value
}