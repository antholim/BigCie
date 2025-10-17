package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.UserType;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthorizationService {
    User getUserFromRequest(HttpServletRequest request);

    boolean hasRole(HttpServletRequest request, UserType requiredRole);

    boolean isOperator(HttpServletRequest request);

    boolean isRider(HttpServletRequest request);

    void requireRole(HttpServletRequest request, UserType requiredRole);

    void requireOperator(HttpServletRequest request);
}