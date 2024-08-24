package css.SimulationRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import css.Consumers.FIFOOrderConsumer;
import css.Interfaces.SimulationRunner;
import css.Model.Courier;
import css.Model.Order;
import css.Producers.CourierSetup;
import css.Producers.KitchenService;
import css.Producers.Waiter;

// Responsible for starting FIFO strategy simulation
public class FIFOSimulationRunner implements SimulationRunner {

    private BlockingQueue<Order> ordersReceivedQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Courier> waitingCourierQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Courier> courierReadyQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Order> readyOrdersQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean allOrdersReceived = new AtomicBoolean(false);
    private AtomicBoolean allOrdersPrepared = new AtomicBoolean(false);
    private AtomicBoolean notifyKitchenAllOrdersProcessed = new AtomicBoolean(false);
    private String filePath;
    private int numCouriers;
    private CourierSetup courierSetup;
    //private String filePath;
    //private int numCouriers;
    //private CourierSetup courierSetup;
    private Waiter waiter;
    private KitchenService kitchenService;
    private FIFOOrderConsumer fifoOrderConsumer;

    public FIFOSimulationRunner(String filePath, int numCouriers) {
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

    // Setters to allow for dependency injection in tests
    public void setCourierSetup(CourierSetup courierSetup) {
        this.courierSetup = courierSetup;
    }

    public void setWaiter(Waiter waiter) {
        this.waiter = waiter;
    }

    public void setKitchenService(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    public void setFIFOOrderConsumer(FIFOOrderConsumer fifoOrderConsumer) {
        this.fifoOrderConsumer = fifoOrderConsumer;
    }

    @Override
    public void run() {
        int totalOrders;

        courierSetup.setUpCouriers(waitingCourierQueue, numCouriers);

        Thread waitorThread = null;
        Thread kitchenServiceThread = null;
        Thread FIFOOrderConsumerThread = null;

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
            FIFOOrderConsumer FIFOOrderConsumer = new FIFOOrderConsumer(readyOrdersQueue, courierReadyQueue, waitingCourierQueue, totalOrders, allOrdersPrepared, notifyKitchenAllOrdersProcessed);
            FIFOOrderConsumerThread = new Thread(FIFOOrderConsumer);
            FIFOOrderConsumerThread.start();

            // Wait for all threads to finish
            System.out.println("Attempting to join threads....");
            waitorThread.join();
            kitchenServiceThread.join();
            FIFOOrderConsumerThread.join();
            System.out.println("Threads have been joined....");
            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public BlockingQueue<Order> getOrdersReceivedQueue() {
        return ordersReceivedQueue;
    }

}
