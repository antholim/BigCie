package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.BillAssembler;
import bigcie.bigcie.constants.prices.PricingConfig;
import bigcie.bigcie.dtos.Billing.BillDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest.PaymentPlanDto;
import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.mappers.PaymentInfoMapper;
import bigcie.bigcie.repositories.BillRepository;
import bigcie.bigcie.services.interfaces.IPaymentService;
import bigcie.bigcie.services.interfaces.IUserService;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService implements IPaymentService {
    private final IUserService userService;
    private final PaymentInfoMapper paymentInfoMapper;
    private final BillAssembler billAssembler;
    private final BillRepository billRepository;
    private final PricingConfig pricingConfig;
    private final IFlexDollarService flexDollarService;

    public PaymentService(IUserService userService, PaymentInfoMapper paymentInfoMapper,
            BillAssembler billAssembler, BillRepository billRepository, PricingConfig pricingConfig,
            IFlexDollarService flexDollarService) {
        this.userService = userService;
        this.paymentInfoMapper = paymentInfoMapper;
        this.billAssembler = billAssembler;
        this.billRepository = billRepository;
        this.pricingConfig = pricingConfig;
        this.flexDollarService = flexDollarService;
    }

    @Override
    public void addPaymentMethod(UUID userId, PaymentInfoRequest paymentInfoRequest) {
        User user = userService.getUserByUUID(userId);

        // Get payment info list - both Rider and DualRoleUser have this
        List<PaymentInfo> paymentInfos = null;
        if (user instanceof Rider rider) {
            paymentInfos = rider.getPaymentInfos();
        } else if (user instanceof DualRoleUser dualRoleUser) {
            paymentInfos = dualRoleUser.getPaymentInfos();
        } else {
            log.warn("Attempt to add payment method for non-rider user: {}", userId);
            throw new IllegalArgumentException("Only riders can add payment methods");
        }

        PaymentInfo paymentInfo = new PaymentInfo();
        // Check if a payment exists already
        if (paymentInfos.isEmpty()) {
            paymentInfo.setDefault(true);
        }

        paymentInfo.setId(UUID.randomUUID());
        paymentInfo.setUserId(user.getId());
        paymentInfo.setCreditCardNumber(paymentInfoRequest.getCreditCardNumber());
        paymentInfo.setCardExpiry(paymentInfoRequest.getCardExpiry());
        paymentInfo.setCardHolderName(paymentInfoRequest.getCardHolderName());
        paymentInfo.setLast4(paymentInfoRequest.getCreditCardNumber().substring(11, 15));
        paymentInfo.setCardType(paymentInfoRequest.getCardType());
        paymentInfo.setCvv(paymentInfoRequest.getCvv());
        paymentInfos.add(paymentInfo);
        userService.updateUser(user);
    }

    @Override
    public void removePaymentMethod(UUID userId, UUID paymentMethodId) {
        return;
    }

    @Override
    public List<PaymentInfoDto> getPaymentInfo(UUID userId) {
        User user = userService.getUserByUUID(userId);
        List<PaymentInfo> paymentInfos = null;
        if (user instanceof Rider rider) {
            paymentInfos = rider.getPaymentInfos();
        } else if (user instanceof DualRoleUser dualRoleUser) {
            paymentInfos = dualRoleUser.getPaymentInfos();
        } else {
            // Non-riders don't have payment info, return empty list
            log.warn("Attempt to get payment info for non-rider user: {}", userId);
            return new ArrayList<>();
        }
        return paymentInfos
                .stream()
                .map(paymentInfoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateDefaultPaymentMethod(UUID userId, UUID paymentMethodId) {
        User user = userService.getUserByUUID(userId);
        List<PaymentInfo> paymentInfos = null;
        if (user instanceof Rider rider) {
            paymentInfos = rider.getPaymentInfos();
        } else if (user instanceof DualRoleUser dualRoleUser) {
            paymentInfos = dualRoleUser.getPaymentInfos();
        } else {
            log.warn("Attempt to update default payment method for non-rider user: {}", userId);
            return; // Silently ignore for non-riders
        }
        for (PaymentInfo paymentInfo : paymentInfos) {
            paymentInfo.setDefault(paymentInfo.getId().equals(paymentMethodId));
        }
    }

    @Override
    public void updatePaymentPlan(UUID userId, PaymentPlanDto paymentPlanRequest) {
        User user = userService.getUserByUUID(userId);
        // Get pricing plan info - both Rider and DualRoleUser have this
        PricingPlanInformation pricingPlanInfo = null;
        PaymentInfo defaultPaymentInfo = null;
        if (user instanceof Rider rider) {
            pricingPlanInfo = rider.getPricingPlanInformation();
            defaultPaymentInfo = rider.getDefaultPaymentInfo();
        } else if (user instanceof DualRoleUser dualRoleUser) {
            pricingPlanInfo = dualRoleUser.getPricingPlanInformation();
            defaultPaymentInfo = dualRoleUser.getDefaultPaymentInfo();
        } else {
            log.warn("Attempt to update payment plan for non-rider user: {}", userId);
            return; // Silently ignore for non-riders
        }
        PricingPlan plan = paymentPlanRequest.getPricingPlan();
        pricingPlanInfo.setPricingPlan(plan);
        switch (plan) {
            case PricingPlan.SINGLE_RIDE -> {
                pricingPlanInfo.setStartDate(null);
                pricingPlanInfo.setEndDate(null);
            }
            case PricingPlan.DAY_PASS -> {
                pricingPlanInfo.setStartDate(LocalDateTime.now());
                pricingPlanInfo.setEndDate(LocalDateTime.now().plusDays(1));
            }
            case PricingPlan.MONTHLY_PASS -> {
                pricingPlanInfo.setStartDate(LocalDateTime.now());
                pricingPlanInfo.setEndDate(LocalDateTime.now().plusMonths(1));
            }
        }
        PlanBill planBill = new PlanBill();
        planBill.setId(UUID.randomUUID());
        planBill.setUserId(user.getId());

        double planCost = pricingConfig.getPriceForPlan(plan);
        planBill.setCost(planCost);

        // Auto-apply flex dollars
        double flexDollarsDeducted = flexDollarService.deductFlexDollars(user.getId(), planCost);
        planBill.setFlexDollarsUsed(flexDollarsDeducted);
        planBill.setAmountCharged(planCost - flexDollarsDeducted);

        planBill.setPricingPlan(plan);
        planBill.setPaymentInfoId(defaultPaymentInfo.getId());

        log.info("Plan {} purchased. Total: ${}, Flex: ${}, Charged: ${}",
                plan, planCost, flexDollarsDeducted, planBill.getAmountCharged());

        billRepository.save(planBill);
        userService.updateUser(user);
    }

    @Override
    public PaymentPlanDto getPricingPlanByUserId(UUID userId) {
        User user = userService.getUserByUUID(userId);
        PricingPlanInformation pricingPlanInfo = null;
        if (user instanceof Rider rider) {
            pricingPlanInfo = rider.getPricingPlanInformation();
        } else if (user instanceof DualRoleUser dualRoleUser) {
            pricingPlanInfo = dualRoleUser.getPricingPlanInformation();
        } else {
            // Non-riders don't have pricing plans, return default single ride plan
            log.warn("Attempt to get pricing plan for non-rider user: {}", userId);
            return new PaymentPlanDto(PricingPlan.SINGLE_RIDE);
        }
        return new PaymentPlanDto(pricingPlanInfo.getPricingPlan());
    }

    @Override
    public List<BillDto> getBillingInfo(UUID userId) {
        List<Bill> bills = billRepository.findByUserId(userId);

        log.info(bills.size() + " total bills found");

        return billAssembler.toBillDtoList(bills, userId);
    }
}
