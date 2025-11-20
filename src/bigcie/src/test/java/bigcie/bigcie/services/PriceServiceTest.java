package bigcie.bigcie.services;

import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PriceServiceTest {

    private final PriceService priceService = new PriceService();

    @Test
    void testCalculatePrice_DayPass_ReturnsZero() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.DAY_PASS, 0);

        // Assert
        assertEquals(0.0, price);
    }

    @Test
    void testCalculatePrice_MonthlyPass_ReturnsZero() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 10, 0);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.MONTHLY_PASS, 0);

        // Assert
        assertEquals(0.0, price);
    }

    @Test
    void testCalculatePrice_RegularBike_5Minutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 5);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertTrue(price > 0);
        assertEquals(priceService.getFiveMinuteRate(), price, 0.01);
    }

    @Test
    void testCalculatePrice_EBike_5Minutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 5);

        // Act
        double regularPrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 0);
        double eBikePrice = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE, PricingPlan.SINGLE_RIDE,
                0);

        // Assert
        assertTrue(eBikePrice > regularPrice);
        assertEquals(priceService.getEBikeSurcharge(), eBikePrice - regularPrice, 0.01);
    }

    @Test
    void testCalculatePrice_RegularBike_10Minutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 10);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        // 10 minutes = 2 units of 5 minutes
        double expectedPrice = 2 * priceService.getFiveMinuteRate();
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_RegularBike_6Minutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 6);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        // 6 minutes = 2 units of 5 minutes (rounded up)
        double expectedPrice = 2 * priceService.getFiveMinuteRate();
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_EBike_WithDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 10);

        // Act
        double priceWithoutDiscount = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE,
                PricingPlan.SINGLE_RIDE, 0);
        double priceWithDiscount = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE,
                PricingPlan.SINGLE_RIDE, 10);

        // Assert
        assertTrue(priceWithDiscount < priceWithoutDiscount);
        assertEquals(priceWithoutDiscount * 0.9, priceWithDiscount, 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 1", // 5 minutes = 1 unit
            "10, 2", // 10 minutes = 2 units
            "15, 3" // 15 minutes = 3 units
    })
    void testCalculatePrice_VariousDurations(int minutes, int expectedUnits) {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(minutes);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        double expectedPrice = expectedUnits * priceService.getFiveMinuteRate();
        assertEquals(expectedPrice, price, 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1.0", // No discount
            "10, 0.9", // 10% discount
            "20, 0.8", // 20% discount
            "50, 0.5" // 50% discount
    })
    void testCalculatePrice_WithVariousDiscounts(int discount, double multiplier) {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = startTime.plusMinutes(5);

        // Act
        double basePrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE,
                0);
        double discountedPrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, discount);

        // Assert
        assertEquals(basePrice * multiplier, discountedPrice, 0.01);
    }

    @Test
    void testCalculatePrice_CrossDayRide() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 23, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 2, 1, 0); // 2 hour ride across days

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertTrue(price > 0);
        // 120 minutes = 24 units of 5 minutes
        double expectedPrice = 24 * priceService.getFiveMinuteRate();
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_LongRide_1Hour() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        double expectedPrice = 12 * priceService.getFiveMinuteRate(); // 60 minutes = 12 units
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_LongRide_2Hours() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 12, 0);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertTrue(price > 0);
        // Verify it's greater than standard bike price for same duration
        double standardPrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 0);
        assertTrue(price > standardPrice);
    }

    @Test
    void testCalculatePrice_SingleRide_5Minutes_StandardBike() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 9, 30);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 9, 35);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertEquals(priceService.getFiveMinuteRate(), price, 0.01);
    }

    @Test
    void testCalculatePrice_SingleRide_NoDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 2, 1, 14, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 2, 1, 14, 15);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        double expectedPrice = 3 * priceService.getFiveMinuteRate(); // 15 minutes
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_SingleRide_With10PercentDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 2, 1, 14, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 2, 1, 14, 15);

        // Act
        double priceWithoutDiscount = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 0);
        double priceWithDiscount = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 10);

        // Assert
        assertEquals(priceWithoutDiscount * 0.9, priceWithDiscount, 0.01);
    }

    @Test
    void testCalculatePrice_EBike_Surcharge_Applied() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 30);

        // Act
        double standardPrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 0);
        double eBikePrice = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE, PricingPlan.SINGLE_RIDE,
                0);

        // Assert
        // E-bike should cost more due to surcharge
        assertTrue(eBikePrice > standardPrice);
        // The difference should be related to the surcharge
        assertTrue(eBikePrice - standardPrice > 0);
    }

    @Test
    void testCalculatePrice_ZeroDuration() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 0); // Same time

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertEquals(0.0, price);
    }

    @Test
    void testCalculatePrice_1MinuteDuration() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 1);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        // 1 minute rounds up to 1 unit (5 minutes)
        assertEquals(priceService.getFiveMinuteRate(), price, 0.01);
    }

    @Test
    void testCalculatePrice_MaxDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 10);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 100); // 100%
                                                                                                                         // discount

        // Assert
        assertEquals(0.0, price);
    }

    @Test
    void testCalculatePrice_IsAccurate_ForMultipleMinutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 25); // 25 minutes

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        // 25 minutes = 5 units of 5 minutes
        double expectedPrice = 5 * priceService.getFiveMinuteRate();
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculatePrice_StandardBike_NoSurcharge() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 5);

        // Act
        double price = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE, 0);

        // Assert
        assertEquals(priceService.getFiveMinuteRate(), price, 0.01);
    }

    @Test
    void testCalculatePrice_HighDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 20);

        // Act
        double basePrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD, PricingPlan.SINGLE_RIDE,
                0);
        double discountedPrice = priceService.calculatePrice(startTime, endTime, BikeType.STANDARD,
                PricingPlan.SINGLE_RIDE, 75);

        // Assert
        assertEquals(basePrice * 0.25, discountedPrice, 0.01);
    }

    @Test
    void testCalculatePrice_EBike_With20PercentDiscount() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 10, 15);

        // Act
        double priceWithoutDiscount = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE,
                PricingPlan.SINGLE_RIDE, 0);
        double priceWithDiscount = priceService.calculatePrice(startTime, endTime, BikeType.E_BIKE,
                PricingPlan.SINGLE_RIDE, 20);

        // Assert
        assertEquals(priceWithoutDiscount * 0.8, priceWithDiscount, 0.01);
    }
}
