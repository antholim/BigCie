package bigcie.bigcie.services;

import bigcie.bigcie.configs.CookieConfigProperties;
import bigcie.bigcie.configs.RtConfigProperties;
import bigcie.bigcie.configs.TokenConfigProperties;
import bigcie.bigcie.models.AuthenticationResponse;
import bigcie.bigcie.models.enums.TokenType;
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
        System.out.println(cookieName);
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println(cookie.getName() + ": " + cookie.getValue());
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    @Override
    public void clearTokenCookie(HttpServletResponse response) {

    }


    @Override
    public void addTokenCookie(HttpServletResponse response, String token, TokenType tokenType, String cookieName) {

        long maxAge = tokenType == TokenType.ACCESS_TOKEN ?
                tokenConfigProperties.getExp() : rtConfigProperties.getExp();

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(cookieConfigProperties.isHttpOnly())
                .secure(cookieConfigProperties.isSecure())
                .path("/")
                .maxAge(maxAge)
                .build();
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

