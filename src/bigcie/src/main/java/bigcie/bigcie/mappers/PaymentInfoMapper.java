package bigcie.bigcie.mappers;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.entities.PaymentInfo;
import org.springframework.stereotype.Component;

@Component
public class PaymentInfoMapper {
    public PaymentInfoDto toDto(PaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setLastFourCreditCardNumber(paymentInfo.getCreditCardNumber().trim().substring(12, 16));
        dto.setCardExpiry(paymentInfo.getCardExpiry());
        dto.setCardHolderName(paymentInfo.getCardHolderName());
        dto.setCardType(paymentInfo.getCardType());
        dto.setPaymentInfoId(paymentInfo.getId());
        dto.setDefault(paymentInfo.isDefault());
        return dto;
    }
}
