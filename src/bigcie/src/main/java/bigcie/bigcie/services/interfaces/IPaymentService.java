package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.entities.PaymentInfo;

import java.util.UUID;

public interface IPaymentService {
    boolean addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest);

    boolean removePaymentMethod(UUID userId, UUID paymentMethodId);
}
