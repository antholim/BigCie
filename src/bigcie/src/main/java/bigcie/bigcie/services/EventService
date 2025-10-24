package bigcie.bigcie.services;

import bigcie.bigcie.entities.BmsEvent;
import bigcie.bigcie.repositories.BmsEventRepository;
import bigcie.bigcie.services.interfaces.IEventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventService implements IEventService {

    private final BmsEventRepository bmsEventRepository;

    public EventService(BmsEventRepository bmsEventRepository) {
        this.bmsEventRepository = bmsEventRepository;
    }

    @Override
    public void recordStateTransition(String entityType, UUID entityId, String oldState, String newState) {
        BmsEvent event = new BmsEvent();
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setOldState(oldState);
        event.setNewState(newState);
        bmsEventRepository.save(event);
    }

    @Override
    public List<BmsEvent> getEventsByEntity(UUID entityId) {
        return bmsEventRepository.findByEntityId(entityId);
    }

    @Override
    public List<BmsEvent> getAllEvents() {
        return bmsEventRepository.findAll();
    }
}