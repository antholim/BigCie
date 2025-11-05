package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.PlanBill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface PlanBillRepository extends MongoRepository<PlanBill, UUID> {
    List<PlanBill> findByUserId(UUID userId);
}
