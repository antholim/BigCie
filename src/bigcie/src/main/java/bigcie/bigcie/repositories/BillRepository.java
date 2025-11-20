package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface BillRepository extends MongoRepository<Bill, UUID> {
    List<Bill> findByUserId(UUID userId);
}
