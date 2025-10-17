package bigcie.bigcie.services;

import bigcie.bigcie.dtos.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.services.interfaces.IPaymentService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PaymentService implements IPaymentService {
    private final IUserService userService;

    public PaymentService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setId(UUID.randomUUID());
        paymentInfo.setUserId(user.getId());
        paymentInfo.setCreditCardNumber(paymentInfoRequest.getCreditCardNumber());
        paymentInfo.setCardExpiry(paymentInfoRequest.getCardExpiry());
        paymentInfo.setCardHolderName(paymentInfoRequest.getCardHolderName());
        paymentInfo.setLast4(paymentInfoRequest.getCreditCardNumber().substring(11, 15));
        paymentInfo.setCardType(paymentInfoRequest.getCardType());
        paymentInfo.setCvv(paymentInfoRequest.getCvv());
        if (user instanceof Rider) {
            rider = (Rider) user;
            rider.getPaymentInfos().add(paymentInfo);
        }
        return false;
    }

    @Override
    public boolean removePaymentMethod(UUID userId, UUID paymentMethodId) {
        return false;
    }
}
