package css;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Consumers.FIFOOrderConsumer;
import css.Model.Courier;
import css.Model.Order;
import css.Producers.CourierSetup;
import css.Producers.KitchenService;
import css.Producers.Waiter;
import css.SimulationRunner.FIFOSimulationRunner;

public class FIFOSimulationRunnerTest {
    private FIFOSimulationRunner simulationRunner;
    private BlockingQueue<Order> ordersReceivedQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Order> readyOrdersQueue;
    private AtomicBoolean allOrdersReceived;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;

    @BeforeEach
    public void setUp() {
        String filePath = "src/test/resources/small_orders.json";
        int numCouriers = 5;

        // Initialize the simulation runner
        simulationRunner = new FIFOSimulationRunner(filePath, numCouriers);

        // Access the queues and flags via reflection or public getters if available
        ordersReceivedQueue = new LinkedBlockingQueue<>();
        waitingCourierQueue = new LinkedBlockingQueue<>();
        courierReadyQueue = new LinkedBlockingQueue<>();
        readyOrdersQueue = new LinkedBlockingQueue<>();
        allOrdersReceived = new AtomicBoolean(false);
        allOrdersPrepared = new AtomicBoolean(false);
        notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);

        // Set up the runner with the same queues
        simulationRunner.setWaiter(new Waiter(filePath, ordersReceivedQueue, allOrdersReceived));
        simulationRunner.setKitchenService(new KitchenService(ordersReceivedQueue, waitingCourierQueue, courierReadyQueue, readyOrdersQueue, allOrdersReceived, allOrdersPrepared, notifyKitchenAllOrdersProcessed));
        simulationRunner.setFIFOOrderConsumer(new FIFOOrderConsumer(readyOrdersQueue, courierReadyQueue, waitingCourierQueue, 10, allOrdersPrepared, notifyKitchenAllOrdersProcessed));
        simulationRunner.setCourierSetup(new CourierSetup());
    }

    @Test
    public void testRunSimulation() {
        // Run the simulation
        simulationRunner.run();

        // Assertions to verify the state of the queues and flags after the simulation
        assertEquals(false, allOrdersReceived.get());
        assertEquals(false, allOrdersPrepared.get());
        assertEquals(false, notifyKitchenAllOrdersProcessed.get());
        
        // Assert that the queues are in the expected state
        assertEquals(0, ordersReceivedQueue.size());
        assertEquals(0, readyOrdersQueue.size());
        assertEquals(0, waitingCourierQueue.size());
        assertEquals(0, courierReadyQueue.size());
    }
    
}
