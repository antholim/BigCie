package bigcie.bigcie.services;

import bigcie.bigcie.dtos.MyProfileInformation.UserProfileInformationDTO;
import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserByUUID(UUID username) {
        try {
            return userRepository.findById(username).orElse(null);
        } catch (Exception e) {
            log.error("Error fetching user by UUID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public User updateUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public UserProfileInformationDTO getUserProfileInformation(UUID userId) {
        User user = getUserByUUID(userId);
        Rider rider;
        if (user instanceof Rider) {
            rider = (Rider) user;
            UserProfileInformationDTO dto = new UserProfileInformationDTO();
            dto.setLoyaltyTier(rider.getLoyaltyTier());
            return dto;
        }
        return null;
    }
}
