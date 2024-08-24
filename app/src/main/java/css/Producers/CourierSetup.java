package css.Producers;

import java.util.concurrent.BlockingQueue;

import css.Model.Courier;

// This class is responsible for creating the requested number of courier's 
// used by the simulator.
public class CourierSetup {

    public void setUpCouriers(BlockingQueue<Courier> waitingCourierQueue, int numCouriers) {
        System.out.println("Setting up couriers....");
        for (int i = 0; i < numCouriers; i++) {
            waitingCourierQueue.add(new Courier(String.valueOf(i)));
        }
        System.out.println("Total couriers: " + waitingCourierQueue.size());
    }
}
