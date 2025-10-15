package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.User;

import java.util.UUID;

public interface IUserService {
    User getUserByUUID(UUID userId);
}
