package bigcie.bigcie.repositories;

import bigcie.bigcie.entities.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BillRepository extends MongoRepository<Bill, UUID> {
    List<Bill> findByUserId(UUID userId);
}
