package bigcie.bigcie.mappers;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.entities.PaymentInfo;
import org.springframework.stereotype.Component;

@Component
public class PaymentInfoMapper {
    public PaymentInfoDto toDto(PaymentInfo paymentInfo) {
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setLastFourCreditCardNumber(paymentInfo.getCreditCardNumber().trim().substring(11, 15));
        dto.setCardExpiry(paymentInfo.getCardExpiry());
        dto.setCardHolderName(paymentInfo.getCardHolderName());
        dto.setCardType(paymentInfo.getCardType());
        return dto;
    }
}
