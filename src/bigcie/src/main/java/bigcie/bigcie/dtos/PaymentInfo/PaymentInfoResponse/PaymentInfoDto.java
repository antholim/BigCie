package bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse;

import bigcie.bigcie.entities.enums.CreditCardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentInfoDto {
    private String lastFourCreditCardNumber;
    private String cardExpiry; // MM/YY
    private String cardHolderName;
    private CreditCardType cardType; // e.g., "VISA"
}