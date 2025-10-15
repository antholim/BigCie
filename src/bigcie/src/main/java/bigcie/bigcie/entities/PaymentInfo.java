package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.CreditCardType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentInfo {
    private UUID userId;
    private String creditCardNumber; // Encrypted or tokenized card number
    private String cardExpiry; // MM/YY
    private String cardHolderName;
    private String last4; // Last 4 digits of card
    private CreditCardType cardType; // e.g., "VISA"

}

