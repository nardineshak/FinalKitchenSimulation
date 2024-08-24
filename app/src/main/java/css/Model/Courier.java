package css.Model;

import java.time.Instant;

public class Courier {

    private String id;
    private Instant timeDispatched;
    private Instant arrivalTime;
    private String orderId;

    public Courier(String id){
        this.id = id;
        this.arrivalTime = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Instant getTimeDispatched() {
        return timeDispatched;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
}
