package bigcie.bigcie.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting client IP address from HTTP requests
 */
public class IpAddressUtil {

    private IpAddressUtil() {
        // Utility class
    }

    /**
     * Extract the client IP address from the HTTP request.
     * Handles proxy headers like X-Forwarded-For, X-Real-IP, etc.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    public static String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs, get the first one
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }
}
