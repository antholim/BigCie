package bigcie.bigcie.services.interfaces;

import java.time.LocalDateTime;

public interface IPriceService {
    double calculatePrice(LocalDateTime startTime, LocalDateTime endTime);
}
