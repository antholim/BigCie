package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.BmsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BmsEventRepository extends MongoRepository<BmsEvent, UUID> {
    List<BmsEvent> findByEntityId(UUID entityId);
}