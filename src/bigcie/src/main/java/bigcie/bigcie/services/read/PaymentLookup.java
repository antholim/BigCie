package bigcie.bigcie.services.read;

import bigcie.bigcie.entities.DualRoleUser;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.services.interfaces.IUserService;
import bigcie.bigcie.services.read.interfaces.IPaymentLookup;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentLookup implements IPaymentLookup {
    private final IUserService userService;

    public PaymentLookup(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public PaymentInfo getPaymentInfo(UUID id, UUID userId) {
        User user = userService.getUserByUUID(userId);
        if (user instanceof Rider rider) {
            for (PaymentInfo paymentInfo : rider.getPaymentInfos()) {
                if (paymentInfo.getId().equals(id)) {
                    return paymentInfo;
                }
            }
        } else if (user instanceof DualRoleUser dualRoleUser) {
            for (PaymentInfo paymentInfo : dualRoleUser.getPaymentInfos()) {
                if (paymentInfo.getId().equals(id)) {
                    return paymentInfo;
                }
            }
        }
        return null;
    }
}
