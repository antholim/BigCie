package bigcie.bigcie.services.read.interfaces;

import bigcie.bigcie.entities.PaymentInfo;

import java.util.UUID;

public interface IPaymentLookup {
    PaymentInfo getPaymentInfo(UUID id, UUID userId);
}
