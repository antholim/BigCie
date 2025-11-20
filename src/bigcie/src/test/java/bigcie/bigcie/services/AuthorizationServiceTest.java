package bigcie.bigcie.services;

import bigcie.bigcie.entities.DualRoleUser;
import bigcie.bigcie.entities.Operator;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private ICookieService cookieService;

    @Mock
    private ITokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private HttpServletRequest request;
    private UUID userId;
    private String token;
    private Rider testRider;
    private Operator testOperator;
    private DualRoleUser testDualRoleUser;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        userId = UUID.randomUUID();
        token = "test-token";

        testRider = new Rider();
        testRider.setId(userId);
        testRider.setType(UserType.RIDER);

        testOperator = new Operator();
        testOperator.setId(userId);
        testOperator.setType(UserType.OPERATOR);

        testDualRoleUser = new DualRoleUser();
        testDualRoleUser.setId(userId);
        testDualRoleUser.setType(UserType.DUAL_ROLE);
    }

    @Test
    void testGetUserFromRequest_Success() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act
        User result = authorizationService.getUserFromRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(UserType.RIDER, result.getType());
    }

    @Test
    void testGetUserFromRequest_NoToken() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorizationService.getUserFromRequest(request));
    }

    @Test
    void testGetUserFromRequest_UserNotFound() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorizationService.getUserFromRequest(request));
    }

    @Test
    void testHasRole_RiderHasRiderRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.RIDER);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRole_RiderDoesNotHaveOperatorRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.OPERATOR);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasRole_OperatorHasOperatorRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.OPERATOR);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRole_OperatorDoesNotHaveRiderRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.RIDER);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasRole_DualRoleHasOperatorRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.OPERATOR);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRole_DualRoleHasRiderRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.RIDER);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRole_DualRoleHasDualRoleRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        boolean result = authorizationService.hasRole(request, UserType.DUAL_ROLE);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasRole_InvalidToken() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(null);

        // Act
        boolean result = authorizationService.hasRole(request, UserType.RIDER);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOperator_WithOperatorRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act
        boolean result = authorizationService.isOperator(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsOperator_WithRiderRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act
        boolean result = authorizationService.isOperator(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOperator_WithDualRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        boolean result = authorizationService.isOperator(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsRider_WithRiderRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act
        boolean result = authorizationService.isRider(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsRider_WithOperatorRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act
        boolean result = authorizationService.isRider(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsRider_WithDualRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        boolean result = authorizationService.isRider(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void testRequireRole_Success() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act & Assert - Should not throw
        authorizationService.requireRole(request, UserType.RIDER);
    }

    @Test
    void testRequireRole_AccessDenied() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorizationService.requireRole(request, UserType.OPERATOR));
    }

    @Test
    void testRequireOperator_Success() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act & Assert - Should not throw
        authorizationService.requireOperator(request);
    }

    @Test
    void testRequireOperator_AccessDenied() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testRider));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorizationService.requireOperator(request));
    }

    @Test
    void testRequireOperator_WithDualRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act & Assert - Should not throw
        authorizationService.requireOperator(request);
    }

    @Test
    void testGetUserFromRequest_WithOperator() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOperator));

        // Act
        User result = authorizationService.getUserFromRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(UserType.OPERATOR, result.getType());
    }

    @Test
    void testGetUserFromRequest_WithDualRole() {
        // Arrange
        when(cookieService.getTokenFromCookie(request, "authToken")).thenReturn(token);
        when(tokenService.extractUserId(token, TokenType.ACCESS_TOKEN)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testDualRoleUser));

        // Act
        User result = authorizationService.getUserFromRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(UserType.DUAL_ROLE, result.getType());
    }
}
