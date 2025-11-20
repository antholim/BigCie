package bigcie.bigcie.services;

import bigcie.bigcie.assemblers.facades.BillAssembler;
import bigcie.bigcie.constants.prices.PricingConfig;
import bigcie.bigcie.dtos.Billing.BillDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoRequest.PaymentInfoRequest;
import bigcie.bigcie.dtos.PaymentInfo.PaymentInfoResponse.PaymentInfoDto;
import bigcie.bigcie.dtos.PaymentInfo.PaymentPlanRequest.PaymentPlanDto;
import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.CreditCardType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.mappers.PaymentInfoMapper;
import bigcie.bigcie.repositories.BillRepository;
import bigcie.bigcie.repositories.PlanBillRepository;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IUserService userService;

    @Mock
    private PaymentInfoMapper paymentInfoMapper;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private BillAssembler billAssembler;

    @Mock
    private BillRepository billRepository;

    @Mock
    private PricingConfig pricingConfig;

    @Mock
    private PlanBillRepository planBillRepository;

    @Mock
    private IFlexDollarService flexDollarService;

    @InjectMocks
    private PaymentService paymentService;

    private Rider testRider;
    private UUID testRiderId;
    private PaymentInfo testPaymentInfo;
    private UUID testPaymentMethodId;

    @BeforeEach
    void setUp() {
        testRiderId = UUID.randomUUID();
        testPaymentMethodId = UUID.randomUUID();

        testRider = new Rider();
        testRider.setId(testRiderId);
        testRider.setPaymentInfos(new ArrayList<>());
        testRider.setPricingPlanInformation(new PricingPlanInformation());
        testRider.getPricingPlanInformation().setPricingPlan(PricingPlan.SINGLE_RIDE);

        testPaymentInfo = new PaymentInfo();
        testPaymentInfo.setId(testPaymentMethodId);
        testPaymentInfo.setUserId(testRiderId);
        testPaymentInfo.setCreditCardNumber("4111111111111111");
        testPaymentInfo.setCardExpiry("12/25");
        testPaymentInfo.setCardHolderName("John Doe");
        testPaymentInfo.setCardType(CreditCardType.VISA);
        testPaymentInfo.setLast4("1111");
        testPaymentInfo.setCvv("123");
        testPaymentInfo.setDefault(true);

        testRider.getPaymentInfos().add(testPaymentInfo);
    }

    @Test
    void testAddPaymentMethod_FirstPaymentMethod() {
        // Arrange
        Rider newRider = new Rider();
        newRider.setId(testRiderId);
        newRider.setPaymentInfos(new ArrayList<>());

        PaymentInfoRequest request = new PaymentInfoRequest();
        request.setCreditCardNumber("4111111111111111");
        request.setCardExpiry("12/25");
        request.setCardHolderName("Jane Doe");
        request.setCardType(CreditCardType.VISA);
        request.setCvv("123");

        when(userService.getUserByUUID(testRiderId)).thenReturn(newRider);

        // Act
        paymentService.addPaymentMethod(testRiderId, request);

        // Assert
        assertEquals(1, newRider.getPaymentInfos().size());
        PaymentInfo addedPaymentInfo = newRider.getPaymentInfos().get(0);
        assertTrue(addedPaymentInfo.isDefault());
        assertEquals("Jane Doe", addedPaymentInfo.getCardHolderName());
        assertEquals("1111", addedPaymentInfo.getLast4());
        verify(userService).updateUser(newRider);
    }

    @Test
    void testAddPaymentMethod_SecondPaymentMethod() {
        // Arrange
        PaymentInfoRequest request = new PaymentInfoRequest();
        request.setCreditCardNumber("5555555555554444");
        request.setCardExpiry("06/26");
        request.setCardHolderName("Jane Doe");
        request.setCardType(CreditCardType.MASTERCARD);
        request.setCvv("456");

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);

        // Act
        paymentService.addPaymentMethod(testRiderId, request);

        // Assert
        assertEquals(2, testRider.getPaymentInfos().size());
        PaymentInfo addedPaymentInfo = testRider.getPaymentInfos().get(1);
        assertFalse(addedPaymentInfo.isDefault());
        assertEquals("Jane Doe", addedPaymentInfo.getCardHolderName());
        assertEquals("5444", addedPaymentInfo.getLast4());
        verify(userService).updateUser(testRider);
    }

    @Test
    void testAddPaymentMethod_DualRoleUser() {
        // Arrange
        DualRoleUser dualRoleUser = new DualRoleUser();
        dualRoleUser.setId(testRiderId);
        dualRoleUser.setPaymentInfos(new ArrayList<>());

        PaymentInfoRequest request = new PaymentInfoRequest();
        request.setCreditCardNumber("4111111111111111");
        request.setCardExpiry("12/25");
        request.setCardHolderName("John Doe");
        request.setCardType(CreditCardType.VISA);
        request.setCvv("123");

        when(userService.getUserByUUID(testRiderId)).thenReturn(dualRoleUser);

        // Act
        paymentService.addPaymentMethod(testRiderId, request);

        // Assert
        assertEquals(1, dualRoleUser.getPaymentInfos().size());
        verify(userService).updateUser(dualRoleUser);
    }

    @Test
    void testAddPaymentMethod_InvalidUser() {
        // Arrange
        Operator operator = new Operator();
        operator.setId(testRiderId);

        PaymentInfoRequest request = new PaymentInfoRequest();
        request.setCreditCardNumber("4111111111111111");
        request.setCardExpiry("12/25");
        request.setCardHolderName("John Doe");
        request.setCardType(CreditCardType.VISA);
        request.setCvv("123");

        when(userService.getUserByUUID(testRiderId)).thenReturn(operator);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paymentService.addPaymentMethod(testRiderId, request));
    }

    @Test
    void testRemovePaymentMethod() {
        // Act
        paymentService.removePaymentMethod(testRiderId, testPaymentMethodId);

        // Assert - method is not implemented (just returns)
        // This test verifies the method can be called without errors
    }

    @Test
    void testGetPaymentInfo_Rider() {
        // Arrange
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setCardHolderName("John Doe");

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);
        when(paymentInfoMapper.toDto(testPaymentInfo)).thenReturn(dto);

        // Act
        List<PaymentInfoDto> paymentInfos = paymentService.getPaymentInfo(testRiderId);

        // Assert
        assertNotNull(paymentInfos);
        assertEquals(1, paymentInfos.size());
        assertEquals("John Doe", paymentInfos.get(0).getCardHolderName());
        verify(paymentInfoMapper).toDto(testPaymentInfo);
    }

    @Test
    void testGetPaymentInfo_DualRoleUser() {
        // Arrange
        DualRoleUser dualRoleUser = new DualRoleUser();
        dualRoleUser.setId(testRiderId);
        dualRoleUser.setPaymentInfos(List.of(testPaymentInfo));

        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setCardHolderName("John Doe");

        when(userService.getUserByUUID(testRiderId)).thenReturn(dualRoleUser);
        when(paymentInfoMapper.toDto(testPaymentInfo)).thenReturn(dto);

        // Act
        List<PaymentInfoDto> paymentInfos = paymentService.getPaymentInfo(testRiderId);

        // Assert
        assertNotNull(paymentInfos);
        assertEquals(1, paymentInfos.size());
        verify(paymentInfoMapper).toDto(testPaymentInfo);
    }

    @Test
    void testGetPaymentInfo_NonRiderUser() {
        // Arrange
        Operator operator = new Operator();
        operator.setId(testRiderId);

        when(userService.getUserByUUID(testRiderId)).thenReturn(operator);

        // Act
        List<PaymentInfoDto> paymentInfos = paymentService.getPaymentInfo(testRiderId);

        // Assert
        assertNotNull(paymentInfos);
        assertEquals(0, paymentInfos.size());
    }

    @Test
    void testUpdateDefaultPaymentMethod_Rider() {
        // Arrange
        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setId(UUID.randomUUID());
        paymentInfo1.setDefault(true);

        PaymentInfo paymentInfo2 = new PaymentInfo();
        paymentInfo2.setId(UUID.randomUUID());
        paymentInfo2.setDefault(false);

        testRider.setPaymentInfos(List.of(paymentInfo1, paymentInfo2));

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);

        // Act
        paymentService.updateDefaultPaymentMethod(testRiderId, paymentInfo2.getId());

        // Assert
        assertFalse(paymentInfo1.isDefault());
        assertTrue(paymentInfo2.isDefault());
    }

    @Test
    void testUpdateDefaultPaymentMethod_DualRoleUser() {
        // Arrange
        DualRoleUser dualRoleUser = new DualRoleUser();
        dualRoleUser.setId(testRiderId);

        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setId(UUID.randomUUID());
        paymentInfo1.setDefault(true);

        PaymentInfo paymentInfo2 = new PaymentInfo();
        paymentInfo2.setId(UUID.randomUUID());
        paymentInfo2.setDefault(false);

        dualRoleUser.setPaymentInfos(List.of(paymentInfo1, paymentInfo2));

        when(userService.getUserByUUID(testRiderId)).thenReturn(dualRoleUser);

        // Act
        paymentService.updateDefaultPaymentMethod(testRiderId, paymentInfo2.getId());

        // Assert
        assertFalse(paymentInfo1.isDefault());
        assertTrue(paymentInfo2.isDefault());
    }

    @Test
    void testUpdateDefaultPaymentMethod_NonRiderUser() {
        // Arrange
        Operator operator = new Operator();
        operator.setId(testRiderId);

        when(userService.getUserByUUID(testRiderId)).thenReturn(operator);

        // Act - should not throw exception
        paymentService.updateDefaultPaymentMethod(testRiderId, testPaymentMethodId);

        // Assert - nothing happens for non-riders
        verify(userService).getUserByUUID(testRiderId);
    }

    @ParameterizedTest
    @EnumSource(PricingPlan.class)
    void testUpdatePaymentPlan_AllPlans(PricingPlan plan) {
        // Arrange
        PaymentPlanDto planDto = new PaymentPlanDto(plan);
        double planCost = 10.0;

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);
        when(pricingConfig.getPriceForPlan(plan)).thenReturn(planCost);
        when(flexDollarService.deductFlexDollars(testRiderId, planCost)).thenReturn(0.0);
        when(billRepository.save(any(Bill.class))).thenReturn(new PlanBill());

        // Act
        paymentService.updatePaymentPlan(testRiderId, planDto);

        // Assert
        assertEquals(plan, testRider.getPricingPlanInformation().getPricingPlan());
        verify(billRepository).save(any(Bill.class));
        verify(userService).updateUser(testRider);
    }

    @Test
    void testUpdatePaymentPlan_WithFlexDollars() {
        // Arrange
        PaymentPlanDto planDto = new PaymentPlanDto(PricingPlan.DAY_PASS);
        double planCost = 7.99;
        double flexDollarsUsed = 5.0;

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);
        when(pricingConfig.getPriceForPlan(PricingPlan.DAY_PASS)).thenReturn(planCost);
        when(flexDollarService.deductFlexDollars(testRiderId, planCost)).thenReturn(flexDollarsUsed);
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            PlanBill bill = invocation.getArgument(0);
            assertEquals(flexDollarsUsed, bill.getFlexDollarsUsed());
            assertEquals(planCost - flexDollarsUsed, bill.getAmountCharged(), 0.01);
            return bill;
        });

        // Act
        paymentService.updatePaymentPlan(testRiderId, planDto);

        // Assert
        verify(billRepository).save(any(Bill.class));
        verify(userService).updateUser(testRider);
    }

    @Test
    void testUpdatePaymentPlan_DualRoleUser() {
        // Arrange
        DualRoleUser dualRoleUser = new DualRoleUser();
        dualRoleUser.setId(testRiderId);
        dualRoleUser.setPricingPlanInformation(new PricingPlanInformation());
        dualRoleUser.getPricingPlanInformation().setPricingPlan(PricingPlan.SINGLE_RIDE);
        dualRoleUser.setPaymentInfos(List.of(testPaymentInfo));

        PaymentPlanDto planDto = new PaymentPlanDto(PricingPlan.MONTHLY_PASS);
        double planCost = 19.99;

        when(userService.getUserByUUID(testRiderId)).thenReturn(dualRoleUser);
        when(pricingConfig.getPriceForPlan(PricingPlan.MONTHLY_PASS)).thenReturn(planCost);
        when(flexDollarService.deductFlexDollars(testRiderId, planCost)).thenReturn(0.0);
        when(billRepository.save(any(Bill.class))).thenReturn(new PlanBill());

        // Act
        paymentService.updatePaymentPlan(testRiderId, planDto);

        // Assert
        assertEquals(PricingPlan.MONTHLY_PASS, dualRoleUser.getPricingPlanInformation().getPricingPlan());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testUpdatePaymentPlan_NonRiderUser() {
        // Arrange
        Operator operator = new Operator();
        operator.setId(testRiderId);

        PaymentPlanDto planDto = new PaymentPlanDto(PricingPlan.DAY_PASS);

        when(userService.getUserByUUID(testRiderId)).thenReturn(operator);

        // Act - should not throw exception
        paymentService.updatePaymentPlan(testRiderId, planDto);

        // Assert - nothing happens for non-riders
        verify(userService).getUserByUUID(testRiderId);
        verify(billRepository, never()).save(any());
    }

    @Test
    void testGetPricingPlanByUserId_Rider() {
        // Arrange
        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);

        // Act
        PaymentPlanDto plan = paymentService.getPricingPlanByUserId(testRiderId);

        // Assert
        assertNotNull(plan);
        assertEquals(PricingPlan.SINGLE_RIDE, plan.getPricingPlan());
    }

    @Test
    void testGetPricingPlanByUserId_DualRoleUser() {
        // Arrange
        DualRoleUser dualRoleUser = new DualRoleUser();
        dualRoleUser.setId(testRiderId);
        PricingPlanInformation pricingPlanInfo = new PricingPlanInformation();
        pricingPlanInfo.setPricingPlan(PricingPlan.MONTHLY_PASS);
        dualRoleUser.setPricingPlanInformation(pricingPlanInfo);

        when(userService.getUserByUUID(testRiderId)).thenReturn(dualRoleUser);

        // Act
        PaymentPlanDto plan = paymentService.getPricingPlanByUserId(testRiderId);

        // Assert
        assertNotNull(plan);
        assertEquals(PricingPlan.MONTHLY_PASS, plan.getPricingPlan());
    }

    @Test
    void testGetPricingPlanByUserId_NonRiderUser() {
        // Arrange
        Operator operator = new Operator();
        operator.setId(testRiderId);

        when(userService.getUserByUUID(testRiderId)).thenReturn(operator);

        // Act
        PaymentPlanDto plan = paymentService.getPricingPlanByUserId(testRiderId);

        // Assert
        assertNotNull(plan);
        assertEquals(PricingPlan.SINGLE_RIDE, plan.getPricingPlan());
    }

    @Test
    void testGetBillingInfo_Success() {
        // Arrange
        Bill bill1 = new PlanBill();
        Bill bill2 = new PlanBill();
        List<Bill> bills = List.of(bill1, bill2);

        BillDto billDto1 = new BillDto();
        BillDto billDto2 = new BillDto();
        List<BillDto> billDtos = List.of(billDto1, billDto2);

        when(billRepository.findByUserId(testRiderId)).thenReturn(bills);
        when(billAssembler.toBillDtoList(bills, testRiderId)).thenReturn(billDtos);

        // Act
        List<BillDto> result = paymentService.getBillingInfo(testRiderId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(billRepository).findByUserId(testRiderId);
        verify(billAssembler).toBillDtoList(bills, testRiderId);
    }

    @Test
    void testGetBillingInfo_Empty() {
        // Arrange
        when(billRepository.findByUserId(testRiderId)).thenReturn(new ArrayList<>());
        when(billAssembler.toBillDtoList(new ArrayList<>(), testRiderId)).thenReturn(new ArrayList<>());

        // Act
        List<BillDto> result = paymentService.getBillingInfo(testRiderId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(billRepository).findByUserId(testRiderId);
    }

    @Test
    void testAddPaymentMethod_DifferentCardTypes() {
        // Arrange
        Rider newRider = new Rider();
        newRider.setId(testRiderId);
        newRider.setPaymentInfos(new ArrayList<>());

        PaymentInfoRequest visaRequest = new PaymentInfoRequest();
        visaRequest.setCreditCardNumber("4111111111111111");
        visaRequest.setCardExpiry("12/25");
        visaRequest.setCardHolderName("Visa User");
        visaRequest.setCardType(CreditCardType.VISA);
        visaRequest.setCvv("123");

        when(userService.getUserByUUID(testRiderId)).thenReturn(newRider);

        // Act
        paymentService.addPaymentMethod(testRiderId, visaRequest);

        // Assert
        assertEquals(1, newRider.getPaymentInfos().size());
        assertEquals(CreditCardType.VISA, newRider.getPaymentInfos().get(0).getCardType());
    }

    @Test
    void testUpdatePaymentPlan_SingleRidePlan() {
        // Arrange
        PaymentPlanDto planDto = new PaymentPlanDto(PricingPlan.SINGLE_RIDE);

        when(userService.getUserByUUID(testRiderId)).thenReturn(testRider);
        when(pricingConfig.getPriceForPlan(PricingPlan.SINGLE_RIDE)).thenReturn(0.0);
        when(flexDollarService.deductFlexDollars(testRiderId, 0.0)).thenReturn(0.0);
        when(billRepository.save(any(Bill.class))).thenReturn(new PlanBill());

        // Act
        paymentService.updatePaymentPlan(testRiderId, planDto);

        // Assert
        assertEquals(PricingPlan.SINGLE_RIDE, testRider.getPricingPlanInformation().getPricingPlan());
        assertNull(testRider.getPricingPlanInformation().getStartDate());
        assertNull(testRider.getPricingPlanInformation().getEndDate());
    }
}
