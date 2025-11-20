package bigcie.bigcie.services.read;

import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.enums.CreditCardType;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PaymentLookup.class)
class PaymentLookupTest {

    @MockBean
    private IUserService userService;

    @Autowired
    private PaymentLookup paymentLookup;

    @Test
    void returnsPaymentInfoForRider() {
        UUID userId = UUID.randomUUID();
        UUID paymentInfoId = UUID.randomUUID();

        PaymentInfo info = new PaymentInfo();
        info.setId(paymentInfoId);
        info.setCreditCardNumber("4111111111111111");
        info.setCardType(CreditCardType.VISA);
        info.setDefault(true);

        Rider rider = new Rider();
        rider.setId(userId);
        rider.getPaymentInfos().add(info);

        when(userService.getUserByUUID(userId)).thenReturn(rider);

        PaymentInfo result = paymentLookup.getPaymentInfo(paymentInfoId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentInfoId);
    }
}
