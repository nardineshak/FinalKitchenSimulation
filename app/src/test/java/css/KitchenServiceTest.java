package css;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Courier;
import css.Model.Order;
import css.Producers.CourierSetup;
import css.Producers.KitchenService;

public class KitchenServiceTest {

    private BlockingQueue<Order> ordersReceivedQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Order> readyOrdersQueue;
    private AtomicBoolean allOrdersReceived;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;

    @BeforeEach
    public void setUp() {
        ordersReceivedQueue = new LinkedBlockingQueue<>();
        waitingCourierQueue = new LinkedBlockingQueue<>();
        courierReadyQueue = new LinkedBlockingQueue<>();
        readyOrdersQueue = new LinkedBlockingQueue<>();
        allOrdersReceived = new AtomicBoolean(false);
        allOrdersPrepared = new AtomicBoolean(false);
        notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);
        CourierSetup courierSetup = new CourierSetup();
        courierSetup.setUpCouriers(waitingCourierQueue, 4);
    }

    @Test
    public void testKitchenServiceWithNoOrders() throws InterruptedException {
        KitchenService kitchenService = new KitchenService(
                ordersReceivedQueue,
                waitingCourierQueue,
                courierReadyQueue,
                readyOrdersQueue,
                allOrdersReceived,
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed
        );

        Thread kitchenThread = new Thread(kitchenService);
        kitchenThread.start();

        // Simulate the condition where no orders are received, but all orders are supposed to be processed
        allOrdersReceived.set(true);
        notifyKitchenAllOrdersProcessed.set(true);

        kitchenThread.join();

        // Verify that all orders were marked as prepared
        assertTrue(allOrdersPrepared.get());
        assertTrue(readyOrdersQueue.isEmpty());
        System.out.println("Test passed: testKitchenServiceWithNoOrders");
    }

    // @Test
    // public void testKitchenServiceWithMultipleOrders() throws InterruptedException {
    //     List<Order> orders = createTestOrders(4);
    //     for (Order order : orders) {
    //         ordersReceivedQueue.add(order);
    //     }
    //     KitchenService kitchenService = new KitchenService(
    //             ordersReceivedQueue,
    //             waitingCourierQueue,
    //             courierReadyQueue,
    //             readyOrdersQueue,
    //             allOrdersReceived,
    //             allOrdersPrepared,
    //             notifyKitchenAllOrdersProcessed
    //     );
    //     Thread kitchenThread = new Thread(kitchenService);
    //     kitchenThread.start();
    //     allOrdersReceived.set(true);
    //     // Simulate that all orders have been received after processing three orders
    //     while (readyOrdersQueue.size() < 4) {
    //         Thread.sleep(100);
    //     }
    //     notifyKitchenAllOrdersProcessed.set(true);
    //     kitchenThread.join(3000);
    //     // Ensure that the couriers are placed back into the courierReadyQueue after delivery
    //     // Verify that the orders were moved to the readyOrdersQueue
    //     assertEquals(readyOrdersQueue.size(), 4, "Not all orders were processed");
    //     // Ensure that the couriers are placed back into the courierReadyQueue after delivery
    //     assertEquals(courierReadyQueue.size(), 3, "Not all couriers were returned to the ready queue");
    //     // Verify that the orders are placed in the readyOrdersQueue
    //     assertEquals(6, readyOrdersQueue.size());
    //     // Verify that all orders were marked as prepared
    //     assertTrue(allOrdersPrepared.get());
    //     System.out.println("Test passed: testKitchenServiceWithMultipleOrders");
    // }
    @Test
    public void testKitchenServiceWithDelayedOrders() throws InterruptedException {
        KitchenService kitchenService = new KitchenService(
                ordersReceivedQueue,
                waitingCourierQueue,
                courierReadyQueue,
                readyOrdersQueue,
                allOrdersReceived,
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed
        );

        Thread kitchenThread = new Thread(kitchenService);
        kitchenThread.start();

        // Simulate delay in receiving orders
        Thread.sleep(1000);
        Order delayedOrder = new Order("1", "pizza", 5);
        ordersReceivedQueue.add(delayedOrder);

        // Signal that all orders have been received and processed after a delay
        Thread.sleep(6000);
        allOrdersReceived.set(true);
        notifyKitchenAllOrdersProcessed.set(true);

        kitchenThread.join();

        // Verify that the delayed order was processed
        assertEquals(1, readyOrdersQueue.size());
        assertEquals(delayedOrder, readyOrdersQueue.poll());

        // Verify that all orders were marked as prepared
        assertTrue(allOrdersPrepared.get());

        System.out.println("Test passed: testKitchenServiceWithDelayedOrders");
    }

    @Test
    public void testKitchenServiceInterruptedException() throws InterruptedException {
        Order testOrder = new Order("1", "pizza", 5);
        ordersReceivedQueue.add(testOrder);

        KitchenService kitchenService = new KitchenService(
                ordersReceivedQueue,
                waitingCourierQueue,
                courierReadyQueue,
                readyOrdersQueue,
                allOrdersReceived,
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed
        );

        Thread kitchenThread = new Thread(kitchenService);
        kitchenThread.start();

        // Interrupt the thread to simulate an interruption
        kitchenThread.interrupt();

        kitchenThread.join();

        // Verify that the kitchen service handled the interruption
        assertTrue(kitchenThread.isInterrupted());
        assertTrue(readyOrdersQueue.isEmpty() || readyOrdersQueue.size() <= 1);

        System.out.println("Test passed: testKitchenServiceInterruptedException");
    }

    @Test
    public void testKitchenServiceWithConcurrentOrderProcessing() throws InterruptedException {
        // Add multiple orders to the queue
        List<Order> orders = createTestOrders(3);

        for (int i = 0; i < 3; i++) {
            ordersReceivedQueue.add(orders.get(i));
        }

        KitchenService kitchenService = new KitchenService(
                ordersReceivedQueue,
                waitingCourierQueue,
                courierReadyQueue,
                readyOrdersQueue,
                allOrdersReceived,
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed
        );

        Thread kitchenThread1 = new Thread(kitchenService);
        Thread kitchenThread2 = new Thread(kitchenService);

        // Start multiple threads to process orders concurrently
        kitchenThread1.start();
        kitchenThread2.start();

        // Simulate that all orders have been received
        allOrdersReceived.set(true);
        Thread.sleep(10000);
        notifyKitchenAllOrdersProcessed.set(true);

        kitchenThread1.join(5000);
        kitchenThread2.join(5000);

        // Verify that all orders are processed and placed in the readyOrdersQueue
        assertEquals(3, readyOrdersQueue.size());
        for (int i = 0; i < 3; i++) {
            assertTrue(readyOrdersQueue.contains(orders.get(i)));
        }

        // Verify that all orders were marked as prepared
        assertTrue(allOrdersPrepared.get());

        System.out.println("Test passed: testKitchenServiceWithConcurrentOrderProcessing");
    }

    // @Test
    // public void testRaceConditionInCourierRequestHandling() throws InterruptedException {
    //     // Prepare the test data
    //     List<Order> orders = createTestOrders(2);
    //     ordersReceivedQueue.addAll(orders);

    //     // Create a latch to ensure both threads complete their work before proceeding
    //     CountDownLatch latch = new CountDownLatch(2);

    //     KitchenService kitchenService1 = new KitchenService(
    //             ordersReceivedQueue,
    //             waitingCourierQueue,
    //             courierReadyQueue,
    //             readyOrdersQueue,
    //             allOrdersReceived,
    //             allOrdersPrepared,
    //             notifyKitchenAllOrdersProcessed
    //     );

    //     KitchenService kitchenService2 = new KitchenService(
    //             ordersReceivedQueue,
    //             waitingCourierQueue,
    //             courierReadyQueue,
    //             readyOrdersQueue,
    //             allOrdersReceived,
    //             allOrdersPrepared,
    //             notifyKitchenAllOrdersProcessed
    //     );

    //     // Override the `run` method to count down the latch when done
    //     Thread kitchenThread1 = new Thread(() -> {
    //         kitchenService1.run();
    //         latch.countDown();  // Signal that this thread has finished
    //     });

    //     Thread kitchenThread2 = new Thread(() -> {
    //         kitchenService2.run();
    //         latch.countDown();  // Signal that this thread has finished
    //     });

    //     // Set the flag to indicate all orders have been received
    //     allOrdersReceived.set(true);

    //     // Start the threads
    //     kitchenThread1.start();
    //     kitchenThread2.start();

    //     // Wait for the orders to be processed by monitoring the ordersReceivedQueue
    //     while (!ordersReceivedQueue.isEmpty()) {
    //         Thread.sleep(1000);  // Allow time for the orders to be processed
    //     }

    //     // Set the flag to indicate that all orders have been processed
    //     notifyKitchenAllOrdersProcessed.set(true);

    //     // Wait for the latch to reach zero, meaning both threads have finished
    //     latch.await(20, TimeUnit.SECONDS);

    //     // Verify that both threads have finished
    //     assertFalse(kitchenThread1.isAlive(), "Thread 1 is still running");
    //     assertFalse(kitchenThread2.isAlive(), "Thread 2 is still running");

    //     // Verify that the orders were moved to the readyOrdersQueue
    //     assertEquals(2, readyOrdersQueue.size(), "Not all orders were processed");

    //     // Ensure that the couriers are placed back into the courierReadyQueue after delivery
    //     // assertEquals(courierReadyQueue.size(), 2, "Not all couriers were returned to the ready queue");
    //     System.out.println("Test passed: testRaceConditionInCourierRequestHandling");
    // }

    @Test
    public void testInterruptedExceptionHandling() throws InterruptedException {
        KitchenService kitchenService = new KitchenService(
                ordersReceivedQueue,
                waitingCourierQueue,
                courierReadyQueue,
                readyOrdersQueue,
                allOrdersReceived,
                allOrdersPrepared,
                notifyKitchenAllOrdersProcessed
        );

        // Capture System.out output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Act
        Thread kitchenThread = new Thread(kitchenService);
        kitchenThread.start();

        // Simulate the interruption after a short delay
        Thread.sleep(100); // Ensure the thread is running before interrupting
        kitchenThread.interrupt(); // Interrupt the kitchen service thread
        kitchenThread.join(); // Wait for the thread to finish

        // Assert
        assertTrue(outputStream.toString().contains("KitchenService completed its work."));
        assertTrue(kitchenThread.isInterrupted());

        // Reset System.out
        System.setOut(System.out);
    }

    public List<Order> createTestOrders(int num) {
        List<Order> orders = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            int prepTime = rand.nextInt(6);
            orders.add(new Order(String.valueOf(i), "", prepTime));
        }
        return orders;
    }

}
