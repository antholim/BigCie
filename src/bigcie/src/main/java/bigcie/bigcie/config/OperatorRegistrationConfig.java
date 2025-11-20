package bigcie.bigcie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "bigcie.operator-registration")
public class OperatorRegistrationConfig {
    /**
     * List of IP addresses allowed to register operators.
     * If empty, all IPs can register operators.
     */
    private List<String> allowedIps = new ArrayList<>();

    /**
     * If true, only IPs in allowedIps list can register operators.
     * If false, IP filtering is disabled.
     */
    private boolean ipFilteringEnabled = false;

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(List<String> allowedIps) {
        this.allowedIps = allowedIps;
    }

    public boolean isIpFilteringEnabled() {
        return ipFilteringEnabled;
    }

    public void setIpFilteringEnabled(boolean ipFilteringEnabled) {
        this.ipFilteringEnabled = ipFilteringEnabled;
    }

    /**
     * Check if the given IP is allowed to register operators
     */
    public boolean isIpAllowed(String clientIp) {
        if (!ipFilteringEnabled) {
            return true;
        }
        return allowedIps.contains(clientIp);
    }
}
