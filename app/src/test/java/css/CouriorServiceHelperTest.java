package css;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import css.Model.Courier;
import css.Producers.CouriorServiceHelper;

public class CouriorServiceHelperTest {

    private BlockingQueue<Courier> courierReadyQueue;
    private Courier courier;

    @BeforeEach
    public void setUp() {
        courierReadyQueue = new LinkedBlockingQueue<>();
        courier = new Courier("courier-1");
    }

    @Test
    public void testStartCourierThreadHandlesInterruptedException() throws InterruptedException {
        int sleepTime = 5000; // 5 seconds

        CouriorServiceHelper helper = new CouriorServiceHelper(sleepTime, courier, courierReadyQueue);

        Thread courierThread = new Thread(helper::startCouriorThread);
        courierThread.start();

        // Interrupt the thread while it's sleeping
        Thread.sleep(1000);  // Sleep for 1 second to ensure the thread is in sleep state
        courierThread.interrupt();

        // Wait for the courier thread to finish
        courierThread.join(1000);

        // Assert that the courier was not added to the ready queue due to interruption
        assertTrue(courierReadyQueue.isEmpty(), "Courier should not be added to the ready queue");
    }

    @Test
    public void testInterruptedExceptionHandling() throws InterruptedException {
        int sleepTime = 5000; // 5 seconds

        CouriorServiceHelper helper = new CouriorServiceHelper(sleepTime, courier, courierReadyQueue);

        Thread courierThread = new Thread(helper::startCouriorThread);
        courierThread.start();

        // Interrupt the thread while it is sleeping
        courierThread.interrupt();

        // Wait for the thread to finish
        courierThread.join();

        // Check if the thread was interrupted
        assertTrue(courierThread.isInterrupted(), "The thread should remain interrupted after handling InterruptedException");

        // Ensure that the courier was not added to the ready queue due to interruption
        assertTrue(courierReadyQueue.isEmpty(), "Courier should not be added to the queue if interrupted");
    }
}
