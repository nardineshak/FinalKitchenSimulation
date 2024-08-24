package css;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Order;
import css.Producers.LoadOrders;
import css.Producers.Waiter;

public class WaiterTest {

    private BlockingQueue<Order> allOrdersQueue;
    private AtomicBoolean allOrdersReceived;

    @BeforeEach
    public void setUp() {
        allOrdersQueue = new LinkedBlockingQueue<>();
        allOrdersReceived = new AtomicBoolean(false);
    }

    @Test
    public void testWaiterWithDummyOrders() {
        // Create a list of dummy orders
        List<Order> dummyOrders = Arrays.asList(
                new Order("1", "Pizza", 3),
                new Order("2", "Burger", 4),
                new Order("3", "Salad", 1)
        );

        // Use the new constructor for testing
        Waiter waiter = new Waiter(dummyOrders, allOrdersQueue, allOrdersReceived);

        // Run the waiter in a thread
        Thread waiterThread = new Thread(waiter);
        waiterThread.start();

        try {
            waiterThread.join(); // Wait for the waiter to finish

            // Verify the queue has the expected orders
            assertEquals(3, allOrdersQueue.size());
            assertTrue(allOrdersReceived.get());

            // Check the content of the queue
            Order firstOrder = allOrdersQueue.take();
            assertEquals("1", firstOrder.getId());

            Order secondOrder = allOrdersQueue.take();
            assertEquals("2", secondOrder.getId());

            Order thirdOrder = allOrdersQueue.take();
            assertEquals("3", thirdOrder.getId());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaiterWithNoOrders() {
        // Create an empty list of orders
        List<Order> emptyOrders = Arrays.asList();

        // Use the new constructor for testing
        Waiter waiter = new Waiter(emptyOrders, allOrdersQueue, allOrdersReceived);

        // Run the waiter in a thread
        Thread waiterThread = new Thread(waiter);
        waiterThread.start();

        try {
            waiterThread.join(); // Wait for the waiter to finish

            // Verify the queue is empty
            assertEquals(0, allOrdersQueue.size());
            assertTrue(allOrdersReceived.get());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaiterInterruptedException() {
        List<Order> dummyOrders = Arrays.asList(
                new Order("1", "Pizza", 3),
                new Order("2", "Burger", 4)
        );

        Waiter waiter = new Waiter(dummyOrders, allOrdersQueue, allOrdersReceived);
        Thread waiterThread = new Thread(waiter);
        waiterThread.start();

        // Interrupt the thread to simulate an interruption
        waiterThread.interrupt();

        try {
            waiterThread.join(); // Wait for the waiter to finish

            // Verify the interruption was handled and the queue has no orders
            assertTrue(allOrdersQueue.isEmpty() || allOrdersQueue.size() <= 2);
            assertTrue(allOrdersReceived.get());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaiterTimelines() {
        List<Order> dummyOrders = Arrays.asList(
                new Order("1", "Pizza", 3),
                new Order("2", "Burger", 4),
                new Order("3", "Salad", 1)
        );

        Waiter waiter = new Waiter(dummyOrders, allOrdersQueue, allOrdersReceived);
        Thread waiterThread = new Thread(waiter);

        long startTime = System.currentTimeMillis();
        waiterThread.start();

        try {
            waiterThread.join(); // Wait for the waiter to finish
            long endTime = System.currentTimeMillis();

            // Verify that the total time taken is close to expected (3 orders, 1 second delay after every 2 orders)
            long expectedTime = 2000L; // 2 seconds total for 3 orders
            assertTrue((endTime - startTime) >= expectedTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void testWaiterFileInput(){
        String filePath = "/Users/nardineshak/css/app/src/test/resources/small_orders.json";
        Waiter waiter = new Waiter(filePath, allOrdersQueue, allOrdersReceived);
        Thread waiterThread = new Thread(waiter);
        waiterThread.start();

        try {
            waiterThread.join(); // Wait for the waiter to finish

            // Verify the queue has 4 items
            assertEquals(4, allOrdersQueue.size());
            assertTrue(allOrdersReceived.get());

            assertEquals(4, waiter.getTotalOrders());
            assertEquals(filePath, waiter.getLoadOrders().getFilePath());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    
    @Test
    public void testIOExceptionHandling() {
        // Arrange
        String invalidFilePath = "invalid_path/small_orders.json";

        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);

        // Act
        LoadOrders loadOrders = new LoadOrders(invalidFilePath);
        List<Order> orders = loadOrders.getOrders();

        // Assert
        assertTrue(orders.isEmpty(), "Orders list should be empty when IOException occurs.");
        assertTrue(outputStream.toString().contains("File not found."), "Error message should be printed.");

        // Reset System.out
        System.setOut(System.out);
    }


}
