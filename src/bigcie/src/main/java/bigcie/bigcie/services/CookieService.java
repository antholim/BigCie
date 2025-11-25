package bigcie.bigcie.services;

import bigcie.bigcie.configs.CookieConfigProperties;
import bigcie.bigcie.configs.RtConfigProperties;
import bigcie.bigcie.configs.TokenConfigProperties;
import bigcie.bigcie.models.AuthenticationResponse;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.services.interfaces.ICookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CookieService implements ICookieService {

    private final TokenConfigProperties tokenConfigProperties;
    private final RtConfigProperties rtConfigProperties;
    private final CookieConfigProperties cookieConfigProperties;

    @Override
    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        System.out.println("Looking for cookie: " + cookieName);
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Found cookie: " + cookie.getName() + ": "
                        + cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())));
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Fallback: Check Authorization header for Bearer token
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Extract token after "Bearer "
            System.out.println("Extracted token from header: " + token.substring(0, Math.min(20, token.length())));
            return token;
        }

        System.out.println("No token found!");
        return null;
    }

    @Override
    public void clearTokenCookie(HttpServletResponse response) {

    }

    @Override
    public void addTokenCookie(HttpServletResponse response, String token, TokenType tokenType, String cookieName) {

        long maxAge = tokenType == TokenType.ACCESS_TOKEN ? tokenConfigProperties.getExp()
                : rtConfigProperties.getExp();

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(cookieConfigProperties.isHttpOnly())
                .secure(cookieConfigProperties.isSecure())
                .path("/")
                .maxAge(maxAge);

        // Add SameSite attribute for cross-origin requests
        if (cookieConfigProperties.getSameSite() != null && !cookieConfigProperties.getSameSite().isEmpty()) {
            builder.sameSite(cookieConfigProperties.getSameSite());
        }

        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void addTokenCookies(HttpServletResponse response, AuthenticationResponse res) {
        addTokenCookie(response, res.getToken(), TokenType.ACCESS_TOKEN, "authToken");
        addTokenCookie(response, res.getToken(), TokenType.REFRESH_TOKEN, "refreshToken");
    }

    @Override
    public void clearTokenCookies(HttpServletResponse response) {

    }
}
