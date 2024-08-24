package css.Consumers;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import css.Interfaces.OrderProcessor;
import css.Model.Courier;
import css.Model.Order;
import css.Model.Statistics;

/**
 * Represents the logic for the fifo dispatch strategy.
 */
public class FIFOOrderConsumer implements OrderProcessor {

    private final BlockingQueue<Order> readyOrdersQueue;
    private final BlockingQueue<Courier> courierReadyQueue;
    private final BlockingQueue<Courier> waitingCourierQueue;
    private final AtomicBoolean allOrdersPrepared;
    private final AtomicBoolean notifyKitchenAllOrdersProcessed;
    private final Statistics stats;
    private final int totalOrders;
    private int processedOrders;
    private static final int MAX_REQUEUE_ATTEMPTS = 3;

    public FIFOOrderConsumer(BlockingQueue<Order> readyOrdersQueue,
            BlockingQueue<Courier> courierReadyQueue,
            BlockingQueue<Courier> waitingCourierQueue,
            int totalOrders,
            AtomicBoolean allOrdersPrepared,
            AtomicBoolean notifyKitchenAllOrdersProcessed) {

        this.readyOrdersQueue = readyOrdersQueue;
        this.courierReadyQueue = courierReadyQueue;
        this.waitingCourierQueue = waitingCourierQueue;
        this.allOrdersPrepared = allOrdersPrepared;
        this.notifyKitchenAllOrdersProcessed = notifyKitchenAllOrdersProcessed;
        this.totalOrders = totalOrders;
        this.processedOrders = 0;
        this.stats = new Statistics();
    }

    @Override
    public void consume() {
        try {
            while (true) {
                if (areAllOrdersProcessed() && readyOrdersQueue.isEmpty()) {
                    notifyCompletion();
                    break;
                }

                if (shouldShutdown()) {
                    shutdown();
                    return;
                }

                Order order = getNextOrder();
                if (order == null) {
                    continue;
                }

                if (!isOrderReady(order)){
                    continue;
                }

                processOrder(order);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            finalizeProcessing();
        }
    }

    /**
     * Determines if the consumer should shut down.
     *
     * @return True if the consumer should shut down, false otherwise.
     */
    private boolean shouldShutdown() {
        return allOrdersPrepared.get() && readyOrdersQueue.isEmpty();
    }


    /**
     * Checks if all orders have been processed.
     */
    private boolean areAllOrdersProcessed() {
        return processedOrders >= totalOrders;
    }


    private Order getNextOrder() throws InterruptedException {
        return readyOrdersQueue.take();
    }

    private boolean isOrderReady(Order order) {
        synchronized (order) {
            return order.isReady() && order.getFinishedTime() != null;
        }
    }

    /**
     * Processes the given order, waits for a courier, and logs relevant
     * information.
     *
     * @param order The order to process.
     */
    private void processOrder(Order order) throws InterruptedException {
        while (true) {
            System.out.println("Order " + order.getId() + " is ready and waiting for a courier...");

            Courier courier = courierReadyQueue.poll(5, TimeUnit.SECONDS); // Wait up to 5 seconds for a courier
            if (courier == null) {
                return;
            }

            long endWaitingTime = Instant.now().toEpochMilli();
            handleOrderDelivery(order, courier, endWaitingTime);
            // Exit the loop once the order is processed
            break; 
        }
    }

    private void handleOrderDelivery(Order order, Courier courier, long endWaitingTime) {
        processedOrders++;
        logOrderProcessingStats(order, courier);

        // Courier has delievered order so it's avaliable for another order
        waitingCourierQueue.add(courier);
        stats.printFulFillmentStats(courier, order, endWaitingTime);
    }

    private void logOrderProcessingStats(Order order, Courier courier) {
        System.out.println("Courier " + courier.getId() + " picked up Order " + order.getId());
    }


    private void notifyCompletion() {
        notifyKitchenAllOrdersProcessed.set(true);
    }

    @Override
    public void shutdown() {
        System.out.println("No more orders and production is complete. Shutting down.");
    }

    /**
     * Finalizes processing, prints final statistics, and logs shutdown.
     */
    @Override
    public void finalizeProcessing() {
        stats.printStatistics();
        System.out.println("FIFOConsumer shutting down...");
    }

    @Override
    public void run() {
        consume();
    }

}
