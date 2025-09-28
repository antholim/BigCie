package bigcie.bigcie.services;

import bigcie.bigcie.services.interfaces.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    @Override
    public String extractUsername(String token) {
        return "";
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return "";
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        return false;
    }

    @Override
    public String generateToken(Long userId, UserDetails userDetails, TokenType tokenType) {
        return "";
    }

    @Override
    public boolean isTokenExpired(String token, TokenType tokenType) {
        return false;
    }

    @Override
    public Date extractExpiration(String token, TokenType tokenType) {
        return null;
    }

    @Override
    public Long extractUserId(String token) {
        return 0;
    }

    @Override
    public Long extractUserId(String token, TokenType tokenType) {
        return 0;
    }
}
