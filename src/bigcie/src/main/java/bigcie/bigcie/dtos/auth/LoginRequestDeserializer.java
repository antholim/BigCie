package bigcie.bigcie.dtos.auth;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class LoginRequestDeserializer extends JsonDeserializer<LoginRequest> {
    @Override
    public LoginRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        LoginRequest req = new LoginRequest();
        if (node.has("username")) {
            req.setUsernameOrEmail(node.get("username").asText());
        } else if (node.has("email")) {
            req.setUsernameOrEmail(node.get("email").asText());
        }
        if (node.has("password")) {
            req.setPassword(node.get("password").asText());
        }
        return req;
    }
}

