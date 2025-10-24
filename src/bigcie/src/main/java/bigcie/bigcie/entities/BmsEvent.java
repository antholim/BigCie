package bigcie.bigcie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "bms_events")
public class BmsEvent {
    @Id
    private UUID id = UUID.randomUUID();

    private String entityType;   // e.g., "BikeStation"
    private UUID entityId;
    private String oldState;
    private String newState;
    private String triggeredBy;
    private LocalDateTime timestamp = LocalDateTime.now();

    // --- getters & setters ---
    public UUID getId() { return id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getOldState() { return oldState; }
    public void setOldState(String oldState) { this.oldState = oldState; }
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}