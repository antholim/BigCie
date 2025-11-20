package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.MyProfileInformation.UserProfileInformationDTO;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.AuthorizationService;
import bigcie.bigcie.services.interfaces.ICookieService;
import bigcie.bigcie.services.interfaces.ITokenService;
import bigcie.bigcie.services.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class UserController {
    private final IUserService userService;
    private final ICookieService cookieService;
    private final ITokenService tokenService;
    private final AuthorizationService authorizationService;

    public UserController(IUserService userService, ICookieService cookieService, ITokenService tokenService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.cookieService = cookieService;
        this.tokenService = tokenService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String token = cookieService.getTokenFromCookie(request, "authToken");
            
            // If no cookie, try Authorization header
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            if (token == null) {
                return ResponseEntity.status(401).body(new HashMap<String, String>() {{
                    put("error", "No authentication token found");
                }});
            }
            
            UUID userId = tokenService.extractUserId(token, bigcie.bigcie.entities.enums.TokenType.ACCESS_TOKEN);
            User user = userService.getUserByUUID(userId);
            
            if (user == null) {
                return ResponseEntity.status(401).body(new HashMap<String, String>() {{
                    put("error", "User not found");
                }});
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("userType", user.getType().toString());
            response.put("type", user.getType().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(new HashMap<String, String>() {{
                put("error", "Unauthorized: " + e.getMessage());
            }});
        }
    }

    @Operation(summary = "My profile informations")
    @GetMapping("/user/my-profile")
    public ResponseEntity<?> myProfileInformation(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        UserProfileInformationDTO userProfileInformationDTO = userService.getUserProfileInformation(userId);
        return ResponseEntity.ok(userProfileInformationDTO);
    }

    @Operation(summary = "Check if user can view all trips")
    @GetMapping("/user/can-view-all-trips")
    public ResponseEntity<?> canViewAllTrips(HttpServletRequest request) {
        try {
            User user = authorizationService.getUserFromRequest(request);
            // DUAL_ROLE and OPERATOR users can view all trips
            boolean canViewAll = user.getType() == UserType.OPERATOR || user.getType() == UserType.DUAL_ROLE;
            
            Map<String, Object> response = new HashMap<>();
            response.put("canViewAll", canViewAll);
            response.put("userType", user.getType().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("canViewAll", false);
            response.put("error", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }
    }
}
