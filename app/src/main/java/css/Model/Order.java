package css.Model;

import java.time.Instant;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Objects;

public class Order {

    private String id;
    private String foodItem;
    private int prepTime;
    private Instant finishedTime;
    private boolean ready;
    private final Lock lock = new ReentrantLock();
    private final Condition orderReadyCondition = lock.newCondition();
    private int requeueAttempts;

    public Order(String id){
        this.id = id;
    }
    
    public Order(String id, String foodItem, int prepTime) {
        this.id = id;
        this.foodItem = foodItem;
        this.prepTime = prepTime;
        this.ready = false;
        this.requeueAttempts = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public Instant getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Instant finishedTime) {
        this.finishedTime = finishedTime;
    }

    public int getRequeueAttempts() {
        return requeueAttempts;
    }

    public void incrementRequeueAttempts() {
        this.requeueAttempts++;
    }

    public boolean isReady() {
        lock.lock();
        try {
            return ready;
        } finally {
            lock.unlock();
        }
    }

    public void setReady(boolean ready) {
        lock.lock();
        try {
            this.ready = ready;
            orderReadyCondition.signalAll(); // Signal that the order is ready
        } finally {
            lock.unlock();
        }
    }

    public void awaitReady() throws InterruptedException {
        lock.lock();
        try {
            while (!ready) {
                orderReadyCondition.await(); // Wait until the order is ready
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return prepTime == order.prepTime &&
                id.equals(order.id) &&
                foodItem.equals(order.foodItem);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, foodItem, prepTime);
    }
}
