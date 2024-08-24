package css.Producers;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;

import css.Model.Courier;

/**
 * Responsible for courier's preparation in a separate thread, which is submitted 
 * to an `executorService` for execution. The thread handling is managed by 
 * `executorService` to avoid blocking the main thread.
 */
public class CouriorServiceHelper {

    private BlockingQueue<Courier> courierReadyQueue;
    private int sleepTime;
    private Courier courier;

    public CouriorServiceHelper(int sleepTime, Courier courier, BlockingQueue<Courier> courierReadyQueue) {
        this.sleepTime = sleepTime;
        this.courier = courier;
        this.courierReadyQueue = courierReadyQueue;
    }

    public void startCouriorThread() {
        new Thread(() -> {
            try {
                Thread.sleep(sleepTime);
                courier.setArrivalTime(Instant.now());
                courierReadyQueue.add(courier);
                System.out.println("Courier " + courier.getId() + " is ready to pickup an order.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
