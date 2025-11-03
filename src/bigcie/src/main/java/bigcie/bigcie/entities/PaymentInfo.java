package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.CreditCardType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Getter
@Setter
public class PaymentInfo {
    @MongoId
    private UUID id;
    private UUID userId;
    private String creditCardNumber; // Encrypted or tokenized card number
    private String cardExpiry; // MM/YY
    private String cardHolderName;
    private String last4; // Last 4 digits of card
    private CreditCardType cardType; // e.g., "VISA"
    private String cvv; // Card Verification Value
    private boolean isDefault = false; // Indicates if this is the default payment method

}
