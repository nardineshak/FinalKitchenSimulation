package css;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Courier;
import css.Model.Order;
import css.Model.Statistics;

public class StatisticsTest {

    private Statistics stats;

    @BeforeEach
    void setUp() {
        stats = new Statistics();
    }

    @Test
    void testInitialization() {
        assertNotNull(stats.getFoodWaitTimes());
        assertNotNull(stats.getCourierWaitTimes());
        assertTrue(stats.getFoodWaitTimes().isEmpty());
        assertTrue(stats.getCourierWaitTimes().isEmpty());
    }

    @Test
    void testCalculateAverageWithEmptyList() {
        List<Long> emptyList = new ArrayList<>();
        long result = stats.calculateAverage(emptyList);
        assertEquals(0, result);
    }

    @Test
    void testCalculateAverageWithSingleItem() {
        List<Long> singleItemList = new ArrayList<>();
        singleItemList.add(100L);
        long result = stats.calculateAverage(singleItemList);
        assertEquals(100, result);
    }

    @Test
    void testCalculateAverageWithMultipleItems() {
        List<Long> multipleItemsList = new ArrayList<>();
        multipleItemsList.add(100L);
        multipleItemsList.add(200L);
        multipleItemsList.add(300L);
        long result = stats.calculateAverage(multipleItemsList);
        assertEquals(200, result);
    }

    @Test
    void testPrintFulFillmentStats() {
        // Create instances of Courier and Order
        Courier courier = new Courier("Courier1");
        courier.setArrivalTime(Instant.ofEpochMilli(5000L));
        Order order = new Order("Order1");
        order.setFinishedTime(Instant.ofEpochMilli(1000L));
        long endWaitingTime = 6000L;  // Adjusted end waiting time to be after the courier arrives.

        Statistics stats = new Statistics();
        stats.printFulFillmentStats(courier, order, endWaitingTime);

        List<Long> foodWaitTimes = stats.getFoodWaitTimes();
        List<Long> courierWaitTimes = stats.getCourierWaitTimes();

        // Check that the correct wait times were recorded
        assertEquals(1, foodWaitTimes.size());
        assertEquals(1, courierWaitTimes.size());
        assertEquals(5000L, foodWaitTimes.get(0)); // endWaitingTime - orderFinishedTime
        assertEquals(1000L, courierWaitTimes.get(0)); // endWaitingTime - courierArrivalTime

        // Optionally, capture and verify the console output if needed
    }

    @Test
    void testPrintStatistics() {
        stats.getFoodWaitTimes().add(100L);
        stats.getFoodWaitTimes().add(200L);
        stats.getCourierWaitTimes().add(300L);
        stats.getCourierWaitTimes().add(400L);

        stats.printStatistics();

        // Similar to the printFulFillmentStats test, verify the console output manually.
    }
    @Test
    void testCalculateAveragePrepTime() {
        // Test data: a list of orders with known preparation times
        List<Long> orders = Arrays.asList(
            4L,
            5L,
            7L
        );

        long sum = 0;
        for (Long num: orders){
            sum += num;
        }

        // Expected average prep time: (4L + 5L + 7L) / 3 = 15.0
        long expectedAverage = sum / 3;

        // Calculate the actual average prep time using the method
        long actualAverage = stats.calculateAverage(orders);

        // Assert that the actual average matches the expected average
        assertEquals(expectedAverage, actualAverage, 0.001, "The average preparation time is incorrect.");
    }

    @Test
    void testCalculateAveragePrepTimeEmptyList() {
        // Test with an empty list of orders
        List<Long> orderTimes = Arrays.asList();

        // Expected average prep time: 0.0
        double expectedAverage = 0.0;

        // Calculate the actual average prep time using the method
        double actualAverage = stats.calculateAverage(orderTimes);

        // Assert that the actual average matches the expected average
        assertEquals(expectedAverage, actualAverage, "The average preparation time for an empty list should be 0.0.");
    }

    @Test
    void testCalculateAveragePrepTimeNullList() {
        // Test with a null list of orders
        List<Long> orderTimes = null;

        // Expected average prep time: 0.0
        double expectedAverage = 0.0;

        // Calculate the actual average prep time using the method
        double actualAverage = stats.calculateAverage(orderTimes);

        // Assert that the actual average matches the expected average
        assertEquals(expectedAverage, actualAverage, "The average preparation time for a null list should be 0.0.");
    }
}
