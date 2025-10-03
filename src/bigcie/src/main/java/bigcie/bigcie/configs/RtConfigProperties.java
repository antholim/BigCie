package bigcie.bigcie.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "rt")
@Component
public class RtConfigProperties {
    private String tokenName;
    private String refreshKey;
    private String secret;
    private long exp;
}