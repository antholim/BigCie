package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.PaymentInfo;

import java.util.UUID;

public interface IPaymentService {
    boolean addPaymentMethod(UUID userId, PaymentInfo paymentInfo);
    boolean removePaymentMethod(UUID userId, UUID paymentMethodId);
}
