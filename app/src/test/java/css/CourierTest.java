package css;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Courier;

public class CourierTest {
    private Courier courier;
    private final String courierId = "courier-123";

    @BeforeEach
    public void setUp() {
        courier = new Courier(courierId);
    }

    @Test
    public void testCourierInitialization() {
        assertNotNull(courier, "Courier object should be created");
        assertEquals(courierId, courier.getId(), "Courier ID should match the provided value");
        assertNotNull(courier.getArrivalTime(), "Arrival time should be initialized with the current time");
    }

    @Test
    public void testSetAndGetArrivalTime() {
        Instant newArrivalTime = Instant.now().plusSeconds(3600); // 1 hour in the future
        courier.setArrivalTime(newArrivalTime);

        assertEquals(newArrivalTime, courier.getArrivalTime(), "Arrival time should match the set value");
    }

    @Test
    public void testSetAndGetOrderId() {
        String orderId = "order-456";
        courier.setOrderId(orderId);

        assertEquals(orderId, courier.getOrderId(), "Order ID should match the set value");
    }

    @Test
    public void testTimeDispatchedInitiallyNull() {
        assertNull(courier.getTimeDispatched(), "Time dispatched should be null initially");
    }
    
}
