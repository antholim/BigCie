package bigcie.bigcie.services;

import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.interfaces.IAuthorizationService;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.ITokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService implements IAuthorizationService {
    private final ICookieService cookieService;
    private final ITokenService tokenService;
    private final UserRepository userRepository;

    public AuthorizationService(ICookieService cookieService, ITokenService tokenService, UserRepository userRepository) {
        this.cookieService = cookieService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public User getUserFromRequest(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "accessToken");
        if (token == null) {
            throw new RuntimeException("No access token found");
        }

        UUID userId = tokenService.extractUserId(token, TokenType.ACCESS_TOKEN);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean hasRole(HttpServletRequest request, UserType requiredRole) {
        try {
            User user = getUserFromRequest(request);
            return user.getType() == requiredRole;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isOperator(HttpServletRequest request) {
        return hasRole(request, UserType.OPERATOR);
    }

    @Override
    public boolean isRider(HttpServletRequest request) {
        return hasRole(request, UserType.RIDER);
    }

    @Override
    public void requireRole(HttpServletRequest request, UserType requiredRole) {
        if (!hasRole(request, requiredRole)) {
            throw new RuntimeException("Access denied. Required role: " + requiredRole);
        }
    }

    @Override
    public void requireOperator(HttpServletRequest request) {
        requireRole(request, UserType.OPERATOR);
    }
}

