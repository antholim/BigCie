package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.models.AuthenticationResponse;
import bigcie.bigcie.entities.enums.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ICookieService {
    String getTokenFromCookie(HttpServletRequest request, String tokenName);

    void clearTokenCookie(HttpServletResponse response);

    void addTokenCookie(HttpServletResponse response, String token, TokenType type, String cookieName);

    void addTokenCookies(HttpServletResponse response, AuthenticationResponse res);

    void clearTokenCookies(HttpServletResponse response);
}