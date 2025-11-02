package bigcie.bigcie.services;

import bigcie.bigcie.dtos.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.services.interfaces.IPaymentService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService implements IPaymentService {
    private final IUserService userService;

    public PaymentService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
        } else {
            throw new IllegalArgumentException("User is not a rider");
        }
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setId(UUID.randomUUID());
        paymentInfo.setUserId(user.getId());
        paymentInfo.setCreditCardNumber(paymentInfoRequest.getCreditCardNumber());
        paymentInfo.setCardExpiry(paymentInfoRequest.getCardExpiry());
        paymentInfo.setCardHolderName(paymentInfoRequest.getCardHolderName());
        paymentInfo.setLast4(paymentInfoRequest.getCreditCardNumber().substring(11, 15));
        paymentInfo.setCardType(paymentInfoRequest.getCardType());
        paymentInfo.setCvv(paymentInfoRequest.getCvv());
        rider.getPaymentInfos().add(paymentInfo);
        userService.updateUser(user);
    }

    @Override
    public void removePaymentMethod(UUID userId, UUID paymentMethodId) {
        return;
    }

    @Override
    public List<PaymentInfo> getPaymentInfo(UUID userId) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
        } else {
            throw new IllegalArgumentException("User is not a rider");
        }
        return rider.getPaymentInfos();
    }
}
