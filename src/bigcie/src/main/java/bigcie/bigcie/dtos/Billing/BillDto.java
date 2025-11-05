package bigcie.bigcie.dtos.Billing;

import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BillDto {
    private UUID id;
    private double cost;
    private PaymentInfoDto paymentInfo;
    private LocalDateTime billingDate;
    private String _billClass;

    public static class Builder {
        private final BillDto billingDto = new BillDto();

        public Builder id(UUID id) {
            billingDto.id = id;
            return this;
        }

        public Builder cost(double cost) {
            billingDto.cost = cost;
            return this;
        }

        public Builder paymentInfo(PaymentInfoDto paymentInfo) {
            billingDto.paymentInfo = paymentInfo;
            return this;
        }

        public Builder billingDate(LocalDateTime billingDate) {
            billingDto.billingDate = billingDate;
            return this;
        }
        public Builder _billClass(String billClass) {
            billingDto._billClass = billClass;
            return this;
        }
        public BillDto build() {
            if (billingDto.id == null) {
                billingDto.id = UUID.randomUUID();
            }
            return billingDto;
        }
    }
}
