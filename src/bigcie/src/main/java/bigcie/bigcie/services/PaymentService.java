package bigcie.bigcie.services;

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
    public boolean addPaymentMethod(UUID userId, PaymentInfo paymentInfo) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
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
