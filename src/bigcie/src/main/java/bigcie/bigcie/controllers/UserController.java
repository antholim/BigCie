package bigcie.bigcie.controllers;

import bigcie.bigcie.dtos.MyProfileInformation.UserProfileInformationDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class UserController {
    private final IUserService userService;
    private final ICookieService cookieService;
    private final ITokenService tokenService;

    public UserController(IUserService userService, ICookieService cookieService, ITokenService tokenService) {
        this.userService = userService;
        this.cookieService = cookieService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "My profile informations")
    @GetMapping("/my-profile")
    public ResponseEntity<?> myProfileInformation(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, "authToken");
        UUID userId = tokenService.extractUserId(token);
        UserProfileInformationDTO userProfileInformationDTO = userService.getUserProfileInformation(userId);
        return ResponseEntity.ok(userProfileInformationDTO);
    }
}
