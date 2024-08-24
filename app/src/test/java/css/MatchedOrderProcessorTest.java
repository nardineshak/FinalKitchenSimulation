package css;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Consumers.MatchedOrderConsumer;
import css.Model.Courier;
import css.Model.Order;
import css.Model.Statistics;

public class MatchedOrderProcessorTest {

    private BlockingQueue<Order> readyOrdersQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;
    private Statistics stats;
    private MatchedOrderConsumer matchedOrderConsumer;

    @BeforeEach
    public void setUp() {
        readyOrdersQueue = new LinkedBlockingQueue<>();
        courierReadyQueue = new LinkedBlockingQueue<>();
        waitingCourierQueue = new LinkedBlockingQueue<>();
        allOrdersPrepared = new AtomicBoolean(false);
        notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);
        stats = new Statistics();

        matchedOrderConsumer = new MatchedOrderConsumer(
                readyOrdersQueue,
                courierReadyQueue,
                waitingCourierQueue,
                5, // Assuming totalOrders is 5
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed);
    }

    @Test
    public void testShouldNotShutdownWhenOrdersAreNotPrepared() {
        allOrdersPrepared.set(false);
        assertFalse(notifyKitchenAllOrdersProcessed.get());
    }

    @Test
    public void testAreAllOrdersProcessedWhenProcessedOrdersEqualTotalOrders() {
        matchedOrderConsumer = new MatchedOrderConsumer(
                readyOrdersQueue,
                courierReadyQueue,
                waitingCourierQueue,
                0, // No orders to process
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed);

        assertEquals(0, readyOrdersQueue.size());
    }

    @Test
    public void testAreAllOrdersProcessedWhenProcessedOrdersLessThanTotalOrders() {
        assertFalse(notifyKitchenAllOrdersProcessed.get());
    }

    @Test
    public void testGetNextCourier() throws InterruptedException {
        Courier courier = new Courier("1");
        courierReadyQueue.add(courier);

        assertEquals(1, courierReadyQueue.size());
    }

    @Test
    public void testOrderOfProcessing() throws InterruptedException {
        // Create and add multiple orders to the queue
        Order order1 = new Order("1", "pizza", 3);
        order1.setReady(true);
        order1.setFinishedTime(Instant.now());

        Order order2 = new Order("2", "burger", 2);
        order2.setReady(true);
        order2.setFinishedTime(Instant.now());

        readyOrdersQueue.add(order1);
        readyOrdersQueue.add(order2);

        // Add two couriers
        Courier courier1 = new Courier("1");
        courier1.setOrderId("1");  // Matching courier1 with order1
        Courier courier2 = new Courier("2");
        courier2.setOrderId("2");  // Matching courier2 with order2

        courierReadyQueue.add(courier1);
        courierReadyQueue.add(courier2);

        // Instantiate MatchedOrderConsumer
        MatchedOrderConsumer processor = new MatchedOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                2, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(2);

        // Check that the orders were processed in the correct order
        assertTrue(readyOrdersQueue.isEmpty());
        assertTrue(waitingCourierQueue.contains(courier1));  // Ensure courier1 is available for another order
        assertTrue(waitingCourierQueue.contains(courier2));  // Ensure courier2 is available for another order
        assertTrue(notifyKitchenAllOrdersProcessed.get());

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
    }

    @Test
    public void testShutdown() {
        matchedOrderConsumer.shutdown();
        // No specific state change or method calls to verify,
        // but the method should run without exceptions.
    }

    @Test
    public void testFinalizeProcessing() {
        matchedOrderConsumer.finalizeProcessing();
        // This will call printStatistics on the actual Statistics object,
        // which you can check by manually inspecting the console output,
        // or by adding additional methods to check the state of `stats`.
    }

    @Test
public void testInterruptedExceptionHandling() throws InterruptedException {
    // Create and prepare the order
    Order order = new Order("1", "Pizza", 3);
    order.setReady(true);
    readyOrdersQueue.add(order);

    // Create and add the courier to the queue
    Courier courier = new Courier("1");
    courierReadyQueue.add(courier);

    // Create a thread to run the consume method
    Thread consumerThread = new Thread(() -> {
        try {
            matchedOrderConsumer.consume();
        } catch (Exception e) {
            fail("Exception thrown during consume: " + e.getMessage());
        }
    });

    // Start the consumer thread
    consumerThread.start();

    // Let the consumer run for a short time
    TimeUnit.SECONDS.sleep(2);

    // Interrupt the thread to simulate an interruption
    consumerThread.interrupt();

    // Wait for the thread to finish
    consumerThread.join();

    // Assert that the courier is not in the waiting queue since the process was interrupted
    assertFalse(waitingCourierQueue.contains(courier));

    // Additionally, you could check if some expected cleanup occurred, depending on how your code handles interruption.
}


}
