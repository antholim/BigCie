package bigcie.bigcie.security;

import bigcie.bigcie.configs.RtConfigProperties;
import bigcie.bigcie.configs.TokenConfigProperties;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.TokenType;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.CookieService;
import bigcie.bigcie.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final CookieService cookieService;
    private final UserDetailsService userDetailsService;
    private final TokenConfigProperties tokenConfigProperties;
    private final RtConfigProperties rtConfigProperties;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwtToken = cookieService.getTokenFromCookie(request, tokenConfigProperties.getTokenName());
            String refreshToken = cookieService.getTokenFromCookie(request, rtConfigProperties.getTokenName());
            UserDetails userDetails = null;

            if (jwtToken != null) {
                userDetails = authenticateUsingJwtToken(jwtToken, request);
            }

            // If JWT authentication failed but refresh token is valid and not blacklisted
            if (userDetails == null && refreshToken != null) {
                userDetails = authenticateUsingRefreshToken(refreshToken, request, response);
            }

            // Continue filter chain if authentication succeeds
            if (userDetails != null) {
                setAuthenticationContext(userDetails, request);
            }
        } catch (Exception ex) {
            log.error("JWT processing error: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails authenticateUsingJwtToken(String jwtToken, HttpServletRequest request) {
        try {
            String username = tokenService.extractUsername(jwtToken, TokenType.ACCESS_TOKEN);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (tokenService.isTokenValid(jwtToken, userDetails, TokenType.ACCESS_TOKEN)) {
                    setAuthenticationContext(userDetails, request);
                    return userDetails;
                }
            }
        } catch (Exception e) {
            log.info("Invalid JWT token: {}", e.getMessage());
        }
        return null;
    }

    private UserDetails authenticateUsingRefreshToken(String refreshToken, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            String username = tokenService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (tokenService.isTokenValid(refreshToken, userDetails, TokenType.REFRESH_TOKEN)) {
                    log.debug("Generating new access token for user: {}", userDetails.getUsername());
                    User user = userRepository.findByUsername(userDetails.getUsername())
                            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: ${}"));
                    String newJwtToken = tokenService.generateToken(user.getId(), userDetails, TokenType.ACCESS_TOKEN);
                    cookieService.addTokenCookie(response, newJwtToken, TokenType.ACCESS_TOKEN, "authToken");
                    setAuthenticationContext(userDetails, request);
                    return userDetails;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to process refresh token: {}", e.getMessage());
        }
        return null;
    }

    private void setAuthenticationContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
