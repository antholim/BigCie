package bigcie.bigcie.controllers;

import bigcie.bigcie.entities.BMSConfig;

import bigcie.bigcie.entities.enums.UserType;
import bigcie.bigcie.services.interfaces.IAuthorizationService;
import bigcie.bigcie.services.interfaces.IBMSConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bms-config")
@Slf4j
@Tag(name = "BMS Config", description = "Operations related to BMS configuration")
public class BMSConfigController {
    private final IBMSConfigService bmsConfigService;
    private final IAuthorizationService authorizationService;

    public BMSConfigController(IBMSConfigService bmsConfigService, IAuthorizationService authorizationService) {
        this.bmsConfigService = bmsConfigService;
        this.authorizationService = authorizationService;
    }

    @Operation(summary = "Create BMS Config with initial data from a JSON file")
    @PostMapping
    public ResponseEntity<BMSConfig> createBMSConfig(@RequestParam String JSONFilePath,
            HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to create BMS config");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BMSConfig createdConfig = bmsConfigService.createConfig(JSONFilePath);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
    }

    @Operation(summary = "Get BMS Config")
    @GetMapping("/config")
    public ResponseEntity<BMSConfig> getBMSConfig(HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to get BMS config");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BMSConfig config = bmsConfigService.getBMSConfig();
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Populate the database with configuration data")
    @PostMapping("/populate-db")
    public ResponseEntity<Void> populateDB(HttpServletRequest request) {
        if (!authorizationService.hasRole(request, UserType.OPERATOR)) {
            log.warn("Unauthorized attempt to populate DB with BMS config data");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bmsConfigService.populateDB();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
