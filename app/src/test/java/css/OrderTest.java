package css;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Order;

public class OrderTest {

    private Order order;
    private final String orderId = "order-123";
    private final String foodItem = "Pizza";
    private final int prepTime = 15;

    @BeforeEach
    public void setUp() {
        order = new Order(orderId, foodItem, prepTime);
    }

    @Test
    public void testOrderInitialization() {
        assertNotNull(order, "Order object should be created");
        assertEquals(orderId, order.getId(), "Order ID should match the provided value");
        assertEquals(foodItem, order.getFoodItem(), "Food item should match the provided value");
        assertEquals(prepTime, order.getPrepTime(), "Preparation time should match the provided value");
        assertFalse(order.isReady(), "Order should not be ready upon initialization");
        assertEquals(0, order.getRequeueAttempts(), "Requeue attempts should be 0 upon initialization");
    }

    @Test
    public void testSetAndGetFinishedTime() {
        Instant finishedTime = Instant.now();
        order.setFinishedTime(finishedTime);

        assertEquals(finishedTime, order.getFinishedTime(), "Finished time should match the set value");
    }

    @Test
    public void testSetAndGetPrepTime() {
        int newPrepTime = 20;
        order.setPrepTime(newPrepTime);

        assertEquals(newPrepTime, order.getPrepTime(), "Preparation time should match the set value");
    }

    @Test
    public void testIncrementRequeueAttempts() {
        assertEquals(0, order.getRequeueAttempts(), "Initial requeue attempts should be 0");
        order.incrementRequeueAttempts();
        assertEquals(1, order.getRequeueAttempts(), "Requeue attempts should increment to 1");
    }

    @Test
    public void testSetAndIsReady() {
        assertFalse(order.isReady(), "Order should initially not be ready");
        order.setReady(true);
        assertTrue(order.isReady(), "Order should be ready after being set to true");
    }

    @Test
    public void testAwaitReady() throws InterruptedException {
        // This test simulates waiting for the order to be marked as ready
        Thread orderThread = new Thread(() -> {
            try {
                order.awaitReady();
            } catch (InterruptedException e) {
                fail("Thread was interrupted while waiting for the order to be ready");
            }
        });

        orderThread.start();

        // Simulate some processing time
        Thread.sleep(500);
        assertFalse(order.isReady(), "Order should not be ready yet");

        // Set the order to ready and check if the thread proceeds
        order.setReady(true);

        orderThread.join(1000);  // Wait for the thread to finish

        assertFalse(orderThread.isAlive(), "Thread should have finished after order is ready");
        assertTrue(order.isReady(), "Order should be ready after setReady(true) is called");
    }

    @Test
    public void testEqualsAndHashCode() {
        Order sameOrder = new Order(orderId, foodItem, prepTime);
        Order differentOrder = new Order("order-456", "Burger", 10);

        assertEquals(order, sameOrder, "Orders with the same ID, food item, and prep time should be equal");
        assertNotEquals(order, differentOrder, "Orders with different ID, food item, or prep time should not be equal");
        assertEquals(order.hashCode(), sameOrder.hashCode(), "Hash codes should match for equal orders");
        assertNotEquals(order.hashCode(), differentOrder.hashCode(), "Hash codes should not match for different orders");
    }
    
}
