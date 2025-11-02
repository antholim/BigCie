package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.entities.PaymentInfo;

import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    void addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest);

    void removePaymentMethod(UUID userId, UUID paymentMethodId);
    List<PaymentInfo> getPaymentInfo(UUID userId);
}
