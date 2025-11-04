package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest.PaymentPlanDto;


import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    void addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest);

    void removePaymentMethod(UUID userId, UUID paymentMethodId);
    List<PaymentInfoDto> getPaymentInfo(UUID userId);
    void updateDefaultPaymentMethod(UUID userId, UUID paymentMethodId);
    void updatePaymentPlan(UUID userId, PaymentPlanDto paymentPlanRequest);
    PaymentPlanDto getPricingPlanByUserId(UUID userId);
}
