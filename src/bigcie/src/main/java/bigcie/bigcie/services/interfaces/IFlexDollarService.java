package bigcie.bigcie.services.interfaces;

import java.util.UUID;

public interface IFlexDollarService {
    /**
     * Award flex dollars to a rider
     * @param userId The ID of the rider
     * @param amount The amount of flex dollars to add
     */
    void addFlexDollars(UUID userId, double amount);

    /**
     * Deduct flex dollars from a rider's balance
     * @param userId The ID of the rider
     * @param amount The maximum amount to deduct
     * @return The actual amount deducted (may be less if insufficient balance)
     */
    double deductFlexDollars(UUID userId, double amount);

    /**
     * Get the current flex dollar balance for a rider
     * @param userId The ID of the rider
     * @return The current flex dollar balance
     */
    double getFlexDollarBalance(UUID userId);

    /**
     * Reset flex dollar balance to 0 for a rider (dev/testing only)
     * @param userId The ID of the rider
     */
    void resetFlexDollars(UUID userId);
}
