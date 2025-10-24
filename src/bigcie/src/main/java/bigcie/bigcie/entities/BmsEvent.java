package bigcie.bigcie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "bms_events")
public class BmsEvent {

    @Id
    private UUID id = UUID.randomUUID();
    private String eventId;
    private String entityType;   // e.g. "BikeStation"
    private UUID entityId;
    private String oldState;
    private String newState;
    private LocalDateTime timestamp = LocalDateTime.now();

    public BmsEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getEventId() { return eventId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getOldState() { return oldState; }
    public void setOldState(String oldState) { this.oldState = oldState; }
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    public LocalDateTime getTimestamp() { return timestamp; }
}