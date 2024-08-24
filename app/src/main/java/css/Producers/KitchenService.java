package css.Producers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import css.Model.Courier;
import css.Model.Order;

// Kitchen is responsible for preparing orders
// dispatching courier
// creating id for courier and order to match on (????)
public class KitchenService implements Runnable {

    private final BlockingQueue<Order> ordersReceivedQueue;
    private final BlockingQueue<Courier> waitingCourierQueue;
    private final BlockingQueue<Courier> courierReadyQueue;
    private final BlockingQueue<Order> readyOrdersQueue;
    private final AtomicBoolean allOrdersReceived;
    private final AtomicBoolean allOrdersPrepared;
    private final AtomicBoolean notifyKitchenAllOrdersProcessed;
    private final ExecutorService executorService;

    public KitchenService(BlockingQueue<Order> ordersReceivedQueue,
            BlockingQueue<Courier> waitingCourierQueue,
            BlockingQueue<Courier> courierReadyQueue,
            BlockingQueue<Order> readyOrdersQueue,
            AtomicBoolean allOrdersReceived,
            AtomicBoolean allOrdersPrepared,
            AtomicBoolean notifyKitchenAllOrdersProcessed) {

        this.ordersReceivedQueue = ordersReceivedQueue;
        this.waitingCourierQueue = waitingCourierQueue;
        this.courierReadyQueue = courierReadyQueue;
        this.readyOrdersQueue = readyOrdersQueue;
        this.allOrdersReceived = allOrdersReceived;
        this.allOrdersPrepared = allOrdersPrepared;
        this.notifyKitchenAllOrdersProcessed = notifyKitchenAllOrdersProcessed;
        this.executorService = Executors.newCachedThreadPool();
    }

    // Processes orders from the queue and prepares them for dispatch.
    private void prepareOrder() {
        try {
            while (true) {
                // Retrieves next available courier. take() blocks until there is one.
                Courier avaliableCourier = waitingCourierQueue.take(); 
                System.out.println(waitingCourierQueue.size());
                Order nextOrder = ordersReceivedQueue.poll(1, TimeUnit.SECONDS);
                if (nextOrder == null) {
                    if (closeKitchen()) {
                        // This signals to FIFO/Matched Consumer to also close thread.
                        allOrdersPrepared.set(true);
                        return;
                    }
                    waitingCourierQueue.add(avaliableCourier);
                    continue;
                }
                
                // setting courier's orderId to order for it to match one in Match strategy
                avaliableCourier.setOrderId(nextOrder.getId()); 
                // Dispatch Courier thread to simulate it's delayed arrival time 
                dispatchCourier(avaliableCourier);
                // Create thread to simulate order's prepare time
                prepareOrder(nextOrder);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("KitchenService completed its work.");
        }
    }

    private void dispatchCourier(Courier courier) {
        int sleepTime = (int) (Math.random() * (15000 - 3000)) + 3000;
        System.out.println("Courier " + courier.getId() + " has been dispatched. It will arrive in " + (sleepTime / 1000) + " seconds.");
        CouriorServiceHelper helper = new CouriorServiceHelper(sleepTime, courier, courierReadyQueue);
        executorService.submit(helper::startCouriorThread);
    }

    private void prepareOrder(Order order) {
        PreparingOrderHelper helper = new PreparingOrderHelper(readyOrdersQueue, order);
        helper.startPrepareOrderThread();
    }

    private boolean closeKitchen() {
        return allOrdersReceived.get() && notifyKitchenAllOrdersProcessed.get();
    }

    @Override
    public void run() {
        prepareOrder();
    }

}