package bigcie.bigcie.services;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.services.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlexDollarServiceTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private FlexDollarService flexDollarService;

    private Rider testRider;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRider = new Rider();
        testRider.setId(testUserId);
        testRider.setFlexDollars(10.0);
    }

    @Test
    void testAddFlexDollars_Success() {
        // Arrange
        when(userService.getUserByUUID(testUserId)).thenReturn(testRider);

        // Act
        flexDollarService.addFlexDollars(testUserId, 5.0);

        // Assert
        assertEquals(15.0, testRider.getFlexDollars());
        verify(userService).updateUser(testRider);
    }

    @Test
    void testDeductFlexDollars_FullAmount() {
        // Arrange
        when(userService.getUserByUUID(testUserId)).thenReturn(testRider);

        // Act
        double deducted = flexDollarService.deductFlexDollars(testUserId, 5.0);

        // Assert
        assertEquals(5.0, deducted);
        assertEquals(5.0, testRider.getFlexDollars());
        verify(userService).updateUser(testRider);
    }

    @Test
    void testDeductFlexDollars_PartialAmount() {
        // Arrange - User has $10, tries to deduct $15
        when(userService.getUserByUUID(testUserId)).thenReturn(testRider);

        // Act
        double deducted = flexDollarService.deductFlexDollars(testUserId, 15.0);

        // Assert - Should only deduct $10 (all available)
        assertEquals(10.0, deducted);
        assertEquals(0.0, testRider.getFlexDollars());
        verify(userService).updateUser(testRider);
    }

    @Test
    void testGetFlexDollarBalance() {
        // Arrange
        when(userService.getUserByUUID(testUserId)).thenReturn(testRider);

        // Act
        double balance = flexDollarService.getFlexDollarBalance(testUserId);

        // Assert
        assertEquals(10.0, balance);
    }

    @Test
    void testAddFlexDollars_NegativeAmount() {
        // Act
        flexDollarService.addFlexDollars(testUserId, -5.0);

        // Assert - Should not call userService at all
        verify(userService, never()).getUserByUUID(any());
        verify(userService, never()).updateUser(any());
    }
}
