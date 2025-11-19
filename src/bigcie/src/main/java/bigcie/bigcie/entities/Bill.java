package bigcie.bigcie.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "bills")
public abstract class Bill {
    @MongoId
    protected UUID id;
    protected UUID userId;
    protected double cost;
    protected UUID paymentInfoId;
    protected LocalDateTime billingDate = LocalDateTime.now();
    protected double flexDollarsUsed = 0.0;
    protected double amountCharged = 0.0;

    @Transient
    @JsonProperty("_class")
    public String getClassDiscriminator() {
        TypeAlias ta = this.getClass().getAnnotation(TypeAlias.class);
        if (ta != null && !ta.value().isEmpty()) {
            return ta.value();
        }
        return this.getClass().getName();
    }
}
