package bigcie.bigcie.services;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.mappers.PaymentInfoMapper;
import bigcie.bigcie.services.interfaces.IPaymentService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService implements IPaymentService {
    private final IUserService userService;
    private final PaymentInfoMapper paymentInfoMapper;

    public PaymentService(IUserService userService, PaymentInfoMapper paymentInfoMapper) {
        this.userService = userService;
        this.paymentInfoMapper = paymentInfoMapper;
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
    public List<PaymentInfoDto> getPaymentInfo(UUID userId) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
        } else {
            throw new IllegalArgumentException("User is not a rider");
        }
        return rider.getPaymentInfos()
                .stream()
                .map(paymentInfoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateDefaultPaymentMethod(UUID userId, UUID paymentMethodId) {
        User user = userService.getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
        } else {
            throw new IllegalArgumentException("User is not a rider");
        }
        for (PaymentInfo paymentInfo : rider.getPaymentInfos()) {
            if (paymentInfo.getId().equals(paymentMethodId)) {
                paymentInfo.setDefault(true);
                break;
            } else {
                paymentInfo.setDefault(false);
            }
        }
    }
}
