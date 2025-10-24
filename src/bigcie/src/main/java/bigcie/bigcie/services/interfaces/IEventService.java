package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.BmsEvent;
import java.util.List;
import java.util.UUID;

public interface IEventService {

    /**
     * Records a state transition for a given entity type and ID.
     *
     * @param entityType The type of entity (e.g., "BikeStation", "Bike").
     * @param entityId   The unique ID of the entity whose state changed.
     * @param oldState   The previous state before transition.
     * @param newState   The new state after transition.
     */
    void recordStateTransition(String entityType, UUID entityId, String oldState, String newState);

    /**
     * Retrieves all events for a specific entity.
     *
     * @param entityId The unique ID of the entity.
     * @return List of BmsEvent objects for the given entity.
     */
    List<BmsEvent> getEventsByEntity(UUID entityId);

    /**
     * Retrieves all events in the system.
     *
     * @return List of all BmsEvent objects.
     */
    List<BmsEvent> getAllEvents();
}