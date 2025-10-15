package bigcie.bigcie.services;

import bigcie.bigcie.configs.RtConfigProperties;
import bigcie.bigcie.configs.TokenConfigProperties;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.services.interfaces.ITokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class TokenService implements ITokenService {
    private final TokenConfigProperties tokenConfigProperties;
    private final RtConfigProperties rtConfigProperties;
    private final SecretKey signInKey;
    private final SecretKey refreshKey;

    public TokenService(TokenConfigProperties tokenConfigProperties, RtConfigProperties rtConfigProperties) {
        this.tokenConfigProperties = tokenConfigProperties;
        log.info("Loaded signInKey length: {}", this.tokenConfigProperties.getSignInKey() != null ? this.tokenConfigProperties.getSignInKey().length() : "null");
        this.signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.tokenConfigProperties.getSignInKey()));
        this.rtConfigProperties = rtConfigProperties;
        log.info("Loaded refreshKey length: {}", this.rtConfigProperties.getRefreshKey() != null ? this.rtConfigProperties.getRefreshKey().length() : "null");
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.rtConfigProperties.getRefreshKey()));
    }


    @Override
    public String extractUsername(String token) {
        return extractUsername(token, TokenType.ACCESS_TOKEN);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    @Override
    public UUID extractUserId(String token) {
        return extractUserId(token, TokenType.ACCESS_TOKEN);
    }

    public UUID extractUserId(String token, TokenType tokenType) {
        return extractClaim(token, claims -> UUID.fromString(claims.get("userId", String.class)), tokenType);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, TokenType tokenType) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UUID userId, UserDetails userDetails, TokenType tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return generateToken(claims, userDetails, tokenType);
    }

    private String generateToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            TokenType tokenType
    ) {
        log.info("Generating token for user: {}", userDetails.getUsername());
        long expirationTimeMillis = (tokenType == TokenType.ACCESS_TOKEN)
                ? tokenConfigProperties.getExp() * 1000L // Convert seconds to milliseconds
                : rtConfigProperties.getExp() * 1000L;

        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(tokenType == TokenType.ACCESS_TOKEN ? signInKey : refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateToken(UserDetails userDetails, TokenType tokenType) {
        // This implementation assumes the userId is not available; returns a token with only username as subject
        Map<String, Object> claims = new HashMap<>();
        try {
            if (userDetails instanceof User) {
                claims.put("userId", ((User) userDetails).getId());
            }
        } catch (Exception e) {
            log.warn("Could not cast from UserDetails to User", e.getMessage());
        }
        return generateToken(claims, userDetails, tokenType);
    }

    public boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        final String username = extractUsername(token, tokenType);
        return (username.equals(userDetails.getUsername())) &&
                !isTokenExpired(token, tokenType);
    }

    public Date extractExpiration(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType);
    }

    public boolean isTokenExpired(String token, TokenType tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    // Helper method to extract all claims from a token
    private Claims extractAllClaims(String token, TokenType tokenType) throws JwtException {
        try {
            return Jwts.parser()
                    .verifyWith(getKey(tokenType))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.info("Token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Could not parse token: {}", e.getMessage());
            throw e;
        }
    }

    private SecretKey getKey(TokenType tokenType) {
        return tokenType == TokenType.ACCESS_TOKEN ? signInKey : refreshKey;
    }
}
