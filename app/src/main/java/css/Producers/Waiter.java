package css.Producers;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import css.Model.Order;

/**
 * Represents a waitor receiving orders and giving it to 
 * a kitchen to prepare. 
 */
public class Waiter implements Runnable { // (Producer)

    private final BlockingQueue<Order> ordersReceivedQueue;
    private LoadOrders loader;
    private final List<Order> orders;
    private final AtomicBoolean allOrdersReceived;

    public Waiter(String filePath,
            BlockingQueue<Order> ordersReceivedQueue,
            AtomicBoolean allOrdersReceived) {

        this.ordersReceivedQueue = ordersReceivedQueue;
        loader = new LoadOrders(filePath);
        orders = loader.getOrders();
        this.allOrdersReceived = allOrdersReceived;
    }

    public Waiter(List<Order> orders,
            BlockingQueue<Order> ordersReceivedQueue,
            AtomicBoolean allOrdersReceived) {

        this.ordersReceivedQueue = ordersReceivedQueue;
        this.orders = orders;
        this.allOrdersReceived = allOrdersReceived;
    }

    /**
     * Responsible for reading the orders 2 every second and placing it in
     * allOrderQueue to get prepared.
     */
    public void readOrders() {
        try {
            System.out.println("Waitor is reading orders...");
            int orderCount = 0;
            while (orderCount < orders.size()) {
                for (int i = 0; i < 2 && orderCount < orders.size(); i++) {
                    Order order = orders.get(orderCount++);
                    System.out.println("Order " + order.getId() + " has been added to ordersReceivedQueue.");
                    ordersReceivedQueue.put(order);
                }
                Thread.sleep(1000); // 2 orders every 1 sec
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            allOrdersReceived.set(true);
            System.out.println("Waitor has completed its work. All orders have been requested.");
        }
    }

    public int getTotalOrders() {
        return orders.size();
    }

    public LoadOrders getLoadOrders() {
        return loader;
    }

    @Override
    public void run() {
        readOrders();
    }
}
