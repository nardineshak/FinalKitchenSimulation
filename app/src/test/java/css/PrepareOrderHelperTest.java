package css;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Order;
import css.Producers.PreparingOrderHelper;

public class PrepareOrderHelperTest {

    private BlockingQueue<Order> readyOrdersQueue;
    private Order order;
    private PreparingOrderHelper preparingOrderHelper;

    @BeforeEach
    public void setUp() {
        readyOrdersQueue = new LinkedBlockingQueue<>(10);
        order = new Order("order-123", "Pizza", 5); // Preparation time of 5 seconds
        preparingOrderHelper = new PreparingOrderHelper(readyOrdersQueue, order);
    }

    @Test
    public void testInterruptedExceptionHandling() throws InterruptedException {
        Thread preparingThread = new Thread(() -> {
            preparingOrderHelper.startPrepareOrderThread();
        });

        preparingThread.start();

        // Interrupt the thread while it is sleeping
        preparingThread.interrupt();

        // Wait for the thread to finish
        preparingThread.join();

        // Check if the thread was interrupted
        assertTrue(preparingThread.isInterrupted(), "The thread should remain interrupted after handling InterruptedException");

        // Ensure that the order was not added to the ready queue due to interruption
        assertTrue(readyOrdersQueue.isEmpty(), "Order should not be added to the queue if interrupted");

        // Ensure that the order was not marked as ready
        assertFalse(order.isReady(), "Order should not be marked as ready if interrupted");

        // Ensure that the finished time was not set
        assertNull(order.getFinishedTime(), "Order finished time should not be set if interrupted");
    }
    
}
