package bigcie.bigcie.services;

import bigcie.bigcie.dtos.MyProfileInformation.UserProfileInformationDTO;
import bigcie.bigcie.entities.DualRoleUser;
import bigcie.bigcie.entities.Operator;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.models.loyalty.state.LoyaltyTier;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private UUID testUserId;
    private Rider testRider;
    private Operator testOperator;
    private DualRoleUser testDualRoleUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        testUserId = UUID.randomUUID();

        // Setup test Rider
        testRider = new Rider.Builder()
                .id(testUserId)
                .username("testRider")
                .email("rider@test.com")
                .password("hashedPassword123")
                .build();
        testRider.setLoyaltyTier(LoyaltyTier.SILVER);
        testRider.setFlexDollars(50.0);

        // Setup test Operator
        testOperator = new Operator.Builder()
                .id(UUID.randomUUID())
                .username("testOperator")
                .email("operator@test.com")
                .password("hashedPassword456")
                .build();

        // Setup test DualRoleUser
        testDualRoleUser = new DualRoleUser.Builder()
                .id(UUID.randomUUID())
                .username("dualRole")
                .email("dual@test.com")
                .password("hashedPassword789")
                .build();
        testDualRoleUser.setLoyaltyTier(LoyaltyTier.GOLD);
    }

    // Tests for getUserByUUID
    @Test
    void testGetUserByUUID_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        User result = userService.getUserByUUID(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("testRider", result.getUsername());
        assertEquals("rider@test.com", result.getEmail());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetUserByUUID_NotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserByUUID(testUserId);

        // Assert
        assertNull(result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetUserByUUID_ExceptionHandling() {
        // Arrange
        when(userRepository.findById(testUserId)).thenThrow(new RuntimeException("Database error"));

        // Act
        User result = userService.getUserByUUID(testUserId);

        // Assert
        assertNull(result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetUserByUUID_WithOperator() {
        // Arrange
        when(userRepository.findById(testOperator.getId())).thenReturn(Optional.of(testOperator));

        // Act
        User result = userService.getUserByUUID(testOperator.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testOperator.getId(), result.getId());
        assertEquals("testOperator", result.getUsername());
        assertEquals(UserType.OPERATOR, result.getType());
    }

    @Test
    void testGetUserByUUID_WithDualRoleUser() {
        // Arrange
        when(userRepository.findById(testDualRoleUser.getId())).thenReturn(Optional.of(testDualRoleUser));

        // Act
        User result = userService.getUserByUUID(testDualRoleUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testDualRoleUser.getId(), result.getId());
        assertEquals("dualRole", result.getUsername());
        assertEquals(UserType.DUAL_ROLE, result.getType());
    }

    // Tests for updateUser
    @Test
    void testUpdateUser_Success() {
        // Arrange
        testRider.setUsername("updatedUsername");
        testRider.setEmail("updated@test.com");
        when(userRepository.save(testRider)).thenReturn(testRider);

        // Act
        User result = userService.updateUser(testRider);

        // Assert
        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("updated@test.com", result.getEmail());
        verify(userRepository).save(testRider);
    }

    @Test
    void testUpdateUser_NullUser() {
        // Arrange - updateUser should handle null gracefully
        // Act & Assert - Should return null without throwing exception
        User result = userService.updateUser(null);
        assertNull(result);
    }

    @Test
    void testUpdateUser_ExceptionHandling() {
        // Arrange
        when(userRepository.save(testRider)).thenThrow(new RuntimeException("Save failed"));

        // Act
        User result = userService.updateUser(testRider);

        // Assert
        assertNull(result);
        verify(userRepository).save(testRider);
    }

    @Test
    void testUpdateUser_FlexDollarsUpdate() {
        // Arrange
        testRider.setFlexDollars(100.0);
        when(userRepository.save(testRider)).thenReturn(testRider);

        // Act
        User result = userService.updateUser(testRider);

        // Assert
        assertNotNull(result);
        assertEquals(100.0, ((Rider) result).getFlexDollars());
        verify(userRepository).save(testRider);
    }

    @Test
    void testUpdateUser_LoyaltyTierUpdate() {
        // Arrange
        testRider.setLoyaltyTier(LoyaltyTier.GOLD);
        when(userRepository.save(testRider)).thenReturn(testRider);

        // Act
        User result = userService.updateUser(testRider);

        // Assert
        assertNotNull(result);
        assertEquals(LoyaltyTier.GOLD, ((Rider) result).getLoyaltyTier());
        verify(userRepository).save(testRider);
    }

    @Test
    void testUpdateUser_WithOperator() {
        // Arrange
        testOperator.setUsername("updatedOperator");
        when(userRepository.save(testOperator)).thenReturn(testOperator);

        // Act
        User result = userService.updateUser(testOperator);

        // Assert
        assertNotNull(result);
        assertEquals("updatedOperator", result.getUsername());
        assertEquals(UserType.OPERATOR, result.getType());
    }

    @Test
    void testUpdateUser_WithDualRoleUser() {
        // Arrange
        testDualRoleUser.setUsername("updatedDual");
        when(userRepository.save(testDualRoleUser)).thenReturn(testDualRoleUser);

        // Act
        User result = userService.updateUser(testDualRoleUser);

        // Assert
        assertNotNull(result);
        assertEquals("updatedDual", result.getUsername());
        assertEquals(UserType.DUAL_ROLE, result.getType());
    }

    // Tests for getUserProfileInformation
    @Test
    void testGetUserProfileInformation_RiderSuccess() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(LoyaltyTier.SILVER, result.getLoyaltyTier());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetUserProfileInformation_RiderWithGoldTier() {
        // Arrange
        testRider.setLoyaltyTier(LoyaltyTier.GOLD);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(LoyaltyTier.GOLD, result.getLoyaltyTier());
    }

    @Test
    void testGetUserProfileInformation_RiderWithGoldTier2() {
        // Arrange
        testRider.setLoyaltyTier(LoyaltyTier.GOLD);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(LoyaltyTier.GOLD, result.getLoyaltyTier());
    }

    @Test
    void testGetUserProfileInformation_RiderWithBronzeTier() {
        // Arrange
        testRider.setLoyaltyTier(LoyaltyTier.BRONZE);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(LoyaltyTier.BRONZE, result.getLoyaltyTier());
    }

    @Test
    void testGetUserProfileInformation_OperatorReturnsNull() {
        // Arrange
        when(userRepository.findById(testOperator.getId())).thenReturn(Optional.of(testOperator));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testOperator.getId());

        // Assert
        assertNull(result);
        verify(userRepository).findById(testOperator.getId());
    }

    @Test
    void testGetUserProfileInformation_DualRoleUserReturnsNull() {
        // Arrange - DualRoleUser is not a Rider, so should return null
        when(userRepository.findById(testDualRoleUser.getId())).thenReturn(Optional.of(testDualRoleUser));

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testDualRoleUser.getId());

        // Assert - Should return null because DualRoleUser is not instanceof Rider
        assertNull(result);
    }

    @Test
    void testGetUserProfileInformation_UserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNull(result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetUserProfileInformation_UserNotFoundReturnsNull() {
        // Arrange - Verify that null user results in null profile
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        UserProfileInformationDTO result = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNull(result);
        verify(userRepository).findById(testUserId);
    }

    // Integration-style tests
    @Test
    void testUpdateAndRetrieveUser() {
        // Arrange
        testRider.setUsername("newUsername");
        testRider.setFlexDollars(150.0);
        when(userRepository.save(testRider)).thenReturn(testRider);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        User updatedUser = userService.updateUser(testRider);
        User retrievedUser = userService.getUserByUUID(testUserId);

        // Assert
        assertNotNull(updatedUser);
        assertNotNull(retrievedUser);
        assertEquals("newUsername", retrievedUser.getUsername());
        assertEquals(150.0, ((Rider) retrievedUser).getFlexDollars());
        verify(userRepository).save(testRider);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testGetProfileInformationAfterUpdate() {
        // Arrange
        testRider.setLoyaltyTier(LoyaltyTier.GOLD);
        when(userRepository.save(testRider)).thenReturn(testRider);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        userService.updateUser(testRider);
        UserProfileInformationDTO profile = userService.getUserProfileInformation(testUserId);

        // Assert
        assertNotNull(profile);
        assertEquals(LoyaltyTier.GOLD, profile.getLoyaltyTier());
        verify(userRepository).save(testRider);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testMultipleUserRetrievals() {
        // Arrange
        UUID userId2 = UUID.randomUUID();
        Rider rider2 = new Rider.Builder()
                .id(userId2)
                .username("rider2")
                .email("rider2@test.com")
                .password("pass456")
                .build();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(rider2));

        // Act
        User user1 = userService.getUserByUUID(testUserId);
        User user2 = userService.getUserByUUID(userId2);

        // Assert
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotEquals(user1.getId(), user2.getId());
        assertEquals("testRider", user1.getUsername());
        assertEquals("rider2", user2.getUsername());
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void testUpdateUserWithDifferentTypes(UserType userType) {
        // Arrange
        User user = switch (userType) {
            case RIDER -> testRider;
            case OPERATOR -> testOperator;
            case DUAL_ROLE -> testDualRoleUser;
        };
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = userService.updateUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(userType, result.getType());
        verify(userRepository).save(user);
    }

    @Test
    void testGetUserByUUID_VerifiesRepositoryCall() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testRider));

        // Act
        userService.getUserByUUID(testUserId);

        // Assert - Verify the repository was called exactly once
        verify(userRepository, times(1)).findById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testUpdateUser_VerifiesRepositoryCall() {
        // Arrange
        when(userRepository.save(testRider)).thenReturn(testRider);

        // Act
        userService.updateUser(testRider);

        // Assert - Verify the repository was called exactly once
        verify(userRepository, times(1)).save(testRider);
        verifyNoMoreInteractions(userRepository);
    }
}
