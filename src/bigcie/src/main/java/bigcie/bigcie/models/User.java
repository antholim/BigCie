package bigcie.bigcie.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "users")
public abstract class User {
    @Id
    private UUID id;
    private String username;
    private String email;
    private String password;
    protected UserType type;
}