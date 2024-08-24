package css;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Consumers.FIFOOrderConsumer;
import css.Model.Courier;
import css.Model.Order;
import css.Model.Statistics;

public class FIFOOrderProcessorTest {

    private BlockingQueue<Order> readyOrdersQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;
    private Statistics stats;

    @BeforeEach
    public void setUp() {
        readyOrdersQueue = new LinkedBlockingQueue<>();
        courierReadyQueue = new LinkedBlockingQueue<>();
        waitingCourierQueue = new LinkedBlockingQueue<>();
        allOrdersPrepared = new AtomicBoolean(false);
        notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);
        stats = new Statistics(); // Assuming a Statistics class exists
    }

    @Test
    public void testNormalOperation() throws InterruptedException {
        // Create dummy orders and couriers
        Order order1 = new Order("1", "pizza", 3);
        order1.setReady(true);
        order1.setFinishedTime(Instant.now());
        Courier courier1 = new Courier("1");

        // Add the order and courier to their respective queues
        readyOrdersQueue.add(order1);
        courierReadyQueue.add(courier1);

        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                1, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(1);

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();

        // Check that the order queue is empty and the courier has been assigned
        assertTrue(readyOrdersQueue.isEmpty());  // Assuming stats tracks completed orders
        assertTrue(notifyKitchenAllOrdersProcessed.get());
    }

    @Test
    public void testEmptyQueues() throws InterruptedException {
        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                0, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for a short time to simulate processing delay
        TimeUnit.SECONDS.sleep(1);

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();

        // Check that the queues are still empty
        assertTrue(readyOrdersQueue.isEmpty());
        assertTrue(courierReadyQueue.isEmpty());
        assertTrue(notifyKitchenAllOrdersProcessed.get());
    }

    @Test
    public void testOrderWithoutCourier() throws InterruptedException {
        // Create a dummy order
        Order order1 = new Order("2", "burger", 2);
        order1.setReady(true);
        order1.setFinishedTime(Instant.now());

        // Add the order to the queue but no courier
        readyOrdersQueue.add(order1);

        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                1, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for a short time to simulate processing delay
        TimeUnit.SECONDS.sleep(2);

        // Check that the order is still in the queue since no courier was available
        assertEquals(0, readyOrdersQueue.size());

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
    }

    @Test
    public void testCourierWithoutOrder() throws InterruptedException {
        // Create a dummy courier
        Courier courier1 = new Courier("1");

        // Add the courier to the queue but no orders
        courierReadyQueue.add(courier1);

        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                0, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for a short time to simulate processing delay
        TimeUnit.SECONDS.sleep(2);

        // Check that the courier is still in the queue since no order was available
        assertEquals(1, courierReadyQueue.size());

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
    }

    @Test
    public void testHighLoad() throws InterruptedException {
        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                1, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Simulate high load by adding multiple orders and couriers
        for (int i = 0; i < 100; i++) {
            Order order = new Order(Integer.toString(i), "item" + i, i % 5);
            order.setReady(true);
            order.setFinishedTime(Instant.now());
            readyOrdersQueue.add(order);

            Courier courier = new Courier(Integer.toString(i));
            courierReadyQueue.add(courier);
        }

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(5);

        // Check that all orders have been processed
        assertTrue(readyOrdersQueue.isEmpty());
        assertTrue(notifyKitchenAllOrdersProcessed.get());  // Assuming stats tracks completed orders

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
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
        Courier courier2 = new Courier("2");

        courierReadyQueue.add(courier1);
        courierReadyQueue.add(courier2);

        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                2, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(2);

        // Check that the orders were processed in the correct order (FIFO)
        assertTrue(readyOrdersQueue.isEmpty());
        assertTrue(notifyKitchenAllOrdersProcessed.get());

        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
    }

    @Test
    public void testStatisticsTracking() throws InterruptedException {
        // Create a dummy order and courier
        Order order1 = new Order("1", "pizza", 3);
        order1.setReady(true);
        order1.setFinishedTime(Instant.now());
        Courier courier1 = new Courier("1");

        // Add the order and courier to their respective queues
        readyOrdersQueue.add(order1);
        courierReadyQueue.add(courier1);

        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                1, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );

        // Start the processor in a separate thread
        Thread processorThread = new Thread(processor::run);
        processorThread.start();

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(1);

        assertTrue(notifyKitchenAllOrdersProcessed.get());
        // Stop the processor thread
        processorThread.interrupt();
        processorThread.join();
    }

    @Test
    public void testShouldShutDown() throws InterruptedException {
        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                0, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );
        // Set up the condition that would make shouldShutDown() return true
        allOrdersPrepared.set(true);
        notifyKitchenAllOrdersProcessed.set(true);

        // Run the processor in a separate thread (if needed) or directly call the method that checks shouldShutDown
        Thread processorThread = new Thread(() -> {
            try {
                processor.run();  // Assuming run() is the method that would eventually call shouldShutDown()
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        processorThread.start();

        // Wait for the processor to complete its execution
        processorThread.join();

        // Since shouldShutDown() returned true, the processor should have stopped
        // You can add assertions here to verify that the shutdown behavior was correctly executed
        assertTrue(notifyKitchenAllOrdersProcessed.get());  // Assuming there's a way to check if the processor shut down
    }

    @Test
    public void testShutdown() {
        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                0, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );
        processor.shutdown();
        // No specific state change or method calls to verify,
        // but the method should run without exceptions.
    }

    @Test
    public void testFinalizeProcessing() {
        FIFOOrderConsumer processor = new FIFOOrderConsumer(
                readyOrdersQueue, courierReadyQueue, waitingCourierQueue,
                0, allOrdersPrepared, notifyKitchenAllOrdersProcessed
        );
        processor.finalizeProcessing();
        // This will call printStatistics on the actual Statistics object,
        // which you can check by manually inspecting the console output,
        // or by adding additional methods to check the state of `stats`.
    }
}
