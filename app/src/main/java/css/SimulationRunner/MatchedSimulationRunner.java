package css.SimulationRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import css.Consumers.MatchedOrderConsumer;
import css.Interfaces.SimulationRunner;
import css.Model.Courier;
import css.Model.Order;
import css.Producers.CourierSetup;
import css.Producers.KitchenService;
import css.Producers.Waiter;

// Responsible for starting Matched strategy simulation
public class MatchedSimulationRunner implements SimulationRunner {

    private BlockingQueue<Order> ordersReceivedQueue;
    private BlockingQueue<Courier> waitingCourierQueue;
    private BlockingQueue<Courier> courierReadyQueue;
    private BlockingQueue<Order> readyOrdersQueue;
    private AtomicBoolean allOrdersReceived;
    private AtomicBoolean allOrdersPrepared;
    private AtomicBoolean notifyKitchenAllOrdersProcessed;
    private String filePath;
    private int numCouriers;
    private CourierSetup courierSetup;
    private MatchedOrderConsumer matchedOrderConsumer;
    private Waiter waiter;
    private KitchenService kitchenService;

    public MatchedSimulationRunner(String filePath, int numCouriers) {
        ordersReceivedQueue = new LinkedBlockingQueue<>();
        waitingCourierQueue = new LinkedBlockingQueue<>();
        courierReadyQueue = new LinkedBlockingQueue<>();
        readyOrdersQueue = new LinkedBlockingQueue<>();
        allOrdersReceived = new AtomicBoolean(false);
        allOrdersPrepared = new AtomicBoolean(false);
        notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);
        this.filePath = filePath;
        this.numCouriers = numCouriers;
        courierSetup = new CourierSetup();
    }

    public void run() {
        int totalOrders;

        courierSetup.setUpCouriers(waitingCourierQueue, numCouriers);

        Thread waitorThread = null;
        Thread kitchenServiceThread = null;
        Thread MatchedrderConsumerThread = null;

        try {
            // Waitor is the PRODUCER, It reads the orders and writes requests for couriers
            Waiter waitor = new Waiter(filePath, ordersReceivedQueue, allOrdersReceived);
            totalOrders = waitor.getTotalOrders();
            waitorThread = new Thread(waitor);
            waitorThread.start();

            // KitchenService is Producer since it's writing readyOrders and readyCouriers
            KitchenService kitchenService = new KitchenService(ordersReceivedQueue, waitingCourierQueue, courierReadyQueue, readyOrdersQueue, allOrdersReceived, allOrdersPrepared, notifyKitchenAllOrdersProcessed);
            kitchenServiceThread = new Thread(kitchenService);
            kitchenServiceThread.start();

            // Reading ready orders and courier so CONSUMER
            MatchedOrderConsumer matchedOrderConsumer = new MatchedOrderConsumer(readyOrdersQueue, courierReadyQueue, waitingCourierQueue, totalOrders, allOrdersPrepared, notifyKitchenAllOrdersProcessed);
            MatchedrderConsumerThread = new Thread(matchedOrderConsumer);
            MatchedrderConsumerThread.start();

            // Wait for all threads to finish
            waitorThread.join();
            kitchenServiceThread.join();
            MatchedrderConsumerThread.join();
            System.out.println("Threads have been joined....");
            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setCourierSetup(CourierSetup mockCourierSetup) {
        courierSetup = mockCourierSetup;
    }

    public void setWaiter(Waiter mockWaiter) {
       waiter = mockWaiter;
    }

    public void setKitchenService(KitchenService mockKitchenService) {
       kitchenService = mockKitchenService;
    }

    public void setMatchedOrderConsumer(MatchedOrderConsumer mockMatchedOrderConsumer) {
        matchedOrderConsumer = mockMatchedOrderConsumer;
    }
    
}
