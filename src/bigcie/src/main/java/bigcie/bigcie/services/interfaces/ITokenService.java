package bigcie.bigcie.services.interfaces;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;

public interface ITokenService {
    String extractUsername(String token);
    String extractUsername(String token, TokenType tokenType);
    boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType);
    String generateToken(Long userId,UserDetails userDetails, TokenType tokenType);
    boolean isTokenExpired(String token, TokenType tokenType);
    Date extractExpiration(String token, TokenType tokenType);
    Long extractUserId(String token);
    Long extractUserId(String token, TokenType tokenType);

}
