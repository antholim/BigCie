package bigcie.bigcie.mappers;

import bigcie.bigcie.dtos.Billing.BillDto;
import bigcie.bigcie.entities.Bill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillMapper {
    public BillDto toDto(Bill bill) {
        BillDto billDto = new BillDto.Builder()
                .id(bill.getId())
                .cost(bill.getCost())
                .billingDate(bill.getBillingDate())
                ._billClass(bill.getClassDiscriminator())
                .build();

        return billDto;
    }

    public List<BillDto> toDtos(List<Bill> bills) {
        return bills.stream().map(this::toDto).toList();
    }
}