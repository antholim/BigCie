package bigcie.bigcie.entities;

import bigcie.bigcie.entities.enums.UserType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "users")
public abstract class User implements UserDetails {
    @MongoId
    private UUID id;
    private String username;
    private String email;
    private String password;
    protected UserType type;
}