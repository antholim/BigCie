package bigcie.bigcie.assemblers.facades;

import bigcie.bigcie.dtos.Billing.BillDto;
import bigcie.bigcie.entities.Bill;
import bigcie.bigcie.entities.PaymentInfo;
import bigcie.bigcie.mappers.BillMapper;
import bigcie.bigcie.mappers.PaymentInfoMapper;
import bigcie.bigcie.services.read.interfaces.IPaymentLookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BillAssembler {
    private final BillMapper billMapper;
    private final IPaymentLookup paymentLookup;
    private final PaymentInfoMapper paymentInfoMapper;
    public BillAssembler(BillMapper billMapper, IPaymentLookup paymentLookup, PaymentInfoMapper paymentInfoMapper) {
        this.billMapper = billMapper;
        this.paymentLookup = paymentLookup;
        this.paymentInfoMapper = paymentInfoMapper;
    }

    public List<BillDto> toBillDtoList(List<Bill> bills, UUID userId) {
        List<BillDto> billDtos = billMapper.toDtos(bills);
        int n = Math.min(bills.size(), billDtos.size());
        for (int i = 0; i < n; i++) {
            Bill bill = bills.get(i);
            BillDto billDto = billDtos.get(i);
            // Enrich with payment info
            PaymentInfo paymentInfo = paymentLookup.getPaymentInfo(bill.getPaymentInfoId(), userId);
            billDto.setPaymentInfo(paymentInfoMapper.toDto(paymentInfo));
        }
        return billDtos;
    }
}
