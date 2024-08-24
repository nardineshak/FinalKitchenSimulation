package css.Producers;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;

import css.Model.Order;

/**
 * Responsible for creating threads to simulate the
 * orders being prepared. Once prepared it's added to 
 * readyOrderQueue and finishedTime and isReady is set.
 */
public class PreparingOrderHelper {

    private final BlockingQueue<Order> readyOrdersQueue;
    private final Order order;

    public PreparingOrderHelper(BlockingQueue<Order> readyOrdersQueue, Order order) {
        this.readyOrdersQueue = readyOrdersQueue;
        this.order = order;
    }

    public void startPrepareOrderThread() {
        new Thread(() -> {
            try {
                System.out.println("Order " + order.getId() + " is getting prepared. It will take " + order.getPrepTime() + " seconds");
                Thread.sleep(order.getPrepTime() * 1000);
                synchronized (order) { // Synchronize access to the order object
                    order.setFinishedTime(Instant.now());
                    order.setReady(true);
                }
                readyOrdersQueue.add(order);
                System.out.println("Order " + order.getId() + " finished at " + order.getFinishedTime().toString());
                System.out.println("Order " + order.getId() + " is ready for pickup.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

}
