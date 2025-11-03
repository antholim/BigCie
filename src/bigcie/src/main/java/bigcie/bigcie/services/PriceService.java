package bigcie.bigcie.services;

import bigcie.bigcie.constants.prices.Prices;
import bigcie.bigcie.services.interfaces.IPriceService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PriceService implements IPriceService {

    @Override
    public double calculatePrice(LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        long units = (long) Math.ceil(minutes / 5.0);
        return units * Prices.PRICE_PER_5_MINUTES;
    }
}
