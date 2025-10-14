package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.enums.TokenType;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;
import java.util.UUID;

public interface ITokenService {
    String extractUsername(String token);
    String extractUsername(String token, TokenType tokenType);
    boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType);
    String generateToken(UserDetails userDetails, TokenType tokenType);
    boolean isTokenExpired(String token, TokenType tokenType);
    Date extractExpiration(String token, TokenType tokenType);
    UUID extractUserId(String token);
    UUID extractUserId(String token, TokenType tokenType);

}
