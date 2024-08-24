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
 * Represents the logic for the matched dispatch strategy.
 */
public class MatchedOrderConsumer implements OrderProcessor {

    private BlockingQueue<Order> readyOrdersQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;
    private Statistics stats;
    private int processedOrders;
    private int totalOrders;
    private final long pollInterval = 500;

    public MatchedOrderConsumer(BlockingQueue<Order> readyOrdersQueue,
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
        stats = new Statistics();
    }

    @Override
    public void consume() {
        try {
            while (true) {
                if (shouldShutdown()) {
                    shutdown();
                    return;
                }

                if (areAllOrdersProcessed()) {
                    notifyCompletion();
                    break;
                }

                Courier courier = getNextCourier();
                Order matchedOrder = findOrderById(courier.getOrderId());

                processOrder(courier, matchedOrder);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted: " + e.getMessage());
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
        return allOrdersPrepared.get() && readyOrdersQueue.isEmpty(); // should shut down when all orders are
    }

    /**
     * Checks if all orders have been processed.
     *
     * @return True if all orders have been processed, false otherwise.
     */
    private boolean areAllOrdersProcessed() {
        return processedOrders >= totalOrders;
    }

    /**
     * Retrieves the next available courier from the queue.
     *
     * @return The next available courier.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    private Courier getNextCourier() throws InterruptedException {
        return courierReadyQueue.take();
    }

    /**
     * Processes the given order with the provided courier. Continues checking
     * the order status until it's ready for processing.
     *
     * @param courier The courier assigned to deliver the order.
     * @param matchedOrder The order to be processed.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    private void processOrder(Courier courier, Order matchedOrder) throws InterruptedException {
        boolean orderReady = false;
        while (!orderReady) {
            // If order is ready, process it
            if (matchedOrder != null && matchedOrder.isReady()) {
                orderReady = true;
                long endWaitingTime = Instant.now().toEpochMilli();
                processMatchedOrder(courier, matchedOrder, endWaitingTime);
            } else {
                System.out.println("Order is not ready yet. Courier " + courier.getId() + " is waiting.");
                TimeUnit.MILLISECONDS.sleep(pollInterval);
                // check if order is ready after sleeping
                matchedOrder = findOrderById(courier.getOrderId());
            }
        }
    }

    

    /**
     * Processes the matched order and logs relevant information.
     *
     * @param courier The courier assigned to deliver the order.
     * @param matchedOrder The order to be processed.
     */
    private void processMatchedOrder(Courier courier, Order matchedOrder, long endWaitingTime) {
        processedOrders++;
        logOrderProcessingStats(matchedOrder, courier);
        // Order has been processed so remove
        readyOrdersQueue.remove(matchedOrder);
        // Courier has delievered order so it's avaliable for another order
        System.out.println("Adding courier back");
        waitingCourierQueue.add(courier); 
        stats.printFulFillmentStats(courier, matchedOrder, endWaitingTime);
    }

    private void logOrderProcessingStats(Order order, Courier courier) {
        System.out.println("Courier " + courier.getId() + " picked up Order " + order.getId());
    }

    /**
     * Notifies that all orders have been processed.
     */
    private void notifyCompletion() {
        notifyKitchenAllOrdersProcessed.set(true);
    }

    @Override
    public void shutdown() {
        System.out.println("No more orders and production is complete. Shutting down.");
    }

    /**
     * Finalizes processing, prints statistics, and logs shutdown information.
     */
    @Override
    public void finalizeProcessing() {
        stats.printStatistics();
        System.out.println("MatchedConsumer shutting down.");
    }

    // If order is in the readyOrderQueue it retrieves
    // it using the order's id.
    private Order findOrderById(String orderId) {
        for (Order order : readyOrdersQueue) {
            if (order.getId().equals(orderId)) {
                System.out.println("Order found. Order id " + order.getId());
                return order;
            }
        }
        return null; 
    }

    @Override
    public void run() {
        consume();
    }

}
