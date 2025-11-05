package bigcie.bigcie.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "bills")
@TypeAlias("bill")
public abstract class Bill {
    @MongoId
    protected UUID id;
    protected UUID userId;
    protected double cost;
    protected UUID paymentInfoId;
}
