package bigcie.bigcie.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "token")
@Component
public class TokenConfigProperties {
    private String tokenName;
    private String signInKey; // This should match token.signInKey
    private String secret; // Ensure it's populated if required
    private long exp;
}
