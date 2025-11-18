package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.dtos.MyProfileInformation.UserProfileInformationDTO;
import bigcie.bigcie.entities.User;

import java.util.UUID;

public interface IUserService {
    User getUserByUUID(UUID userId);
    User updateUser(User user);
    UserProfileInformationDTO getUserProfileInformation(UUID userId);
}
