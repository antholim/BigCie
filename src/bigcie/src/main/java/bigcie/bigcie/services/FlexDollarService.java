package bigcie.bigcie.services;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.entities.User;
import bigcie.bigcie.services.interfaces.IFlexDollarService;
import bigcie.bigcie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class FlexDollarService implements IFlexDollarService {
    private final IUserService userService;

    public FlexDollarService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void addFlexDollars(UUID userId, double amount) {
        if (amount <= 0) {
            log.warn("Attempted to add non-positive flex dollars: {}", amount);
            return;
        }

        User user = userService.getUserByUUID(userId);
        if (!(user instanceof Rider rider)) {
            log.warn("Attempted to add flex dollars to non-rider user: {}", userId);
            return;
        }

        double newBalance = rider.getFlexDollars() + amount;
        rider.setFlexDollars(newBalance);
        userService.updateUser(rider);

        log.info("Added {} flex dollars to rider {}. New balance: {}", amount, userId, newBalance);
    }

    @Override
    public double deductFlexDollars(UUID userId, double amount) {
        if (amount <= 0) {
            log.warn("Attempted to deduct non-positive flex dollars: {}", amount);
            return 0.0;
        }

        User user = userService.getUserByUUID(userId);
        if (!(user instanceof Rider rider)) {
            log.warn("Attempted to deduct flex dollars from non-rider user: {}", userId);
            return 0.0;
        }

        double currentBalance = rider.getFlexDollars();
        double actualDeduction = Math.min(currentBalance, amount);
        double newBalance = currentBalance - actualDeduction;

        rider.setFlexDollars(newBalance);
        userService.updateUser(rider);

        log.info("Deducted {} flex dollars from rider {}. New balance: {}", actualDeduction, userId, newBalance);
        return actualDeduction;
    }

    @Override
    public double getFlexDollarBalance(UUID userId) {
        User user = userService.getUserByUUID(userId);
        if (!(user instanceof Rider rider)) {
            log.warn("Attempted to get flex dollar balance for non-rider user: {}", userId);
            return 0.0;
        }

        return rider.getFlexDollars();
    }

    @Override
    public void resetFlexDollars(UUID userId) {
        User user = userService.getUserByUUID(userId);
        if (!(user instanceof Rider rider)) {
            log.warn("Attempted to reset flex dollars for non-rider user: {}", userId);
            return;
        }

        rider.setFlexDollars(0.0);
        userService.updateUser(rider);
        log.info("Reset flex dollars to 0 for rider {}", userId);
    }
}
