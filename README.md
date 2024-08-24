**RUNNING THE SIMULATION:**

Once you download the zip package and decompress it open it in your IDE. 
Navigate to the Simulator class. Run the main method and leave the 
simulation type you want to run uncommented. Since it was commented
make sure to uncomment it's import as well at the top of the class.

You can adjust the number of couriers you want the system to employ throughout
the simulation. 

**RUNNING THE UNIT TESTS:**

In your IDE you can run the unit tests by navigating to the TEST folder. 
Right click on the TEST folder and press "run tests". 

**Design Approach**
The Producer/Consumer design pattern is an excellent fit for the problem of simulating the fulfillment of delivery orders in a kitchen, particularly due to the nature of the tasks involved and the need to handle multiple processes concurrently. Here's why this approach is well-suited:

### **1. Decoupling of Order Preparation and Courier Dispatching**
The Producer/Consumer pattern allows you to decouple the preparation of orders (Producer) from the dispatch and pickup of those orders by couriers (Consumer). In this problem, the kitchen acts as the Producer, continuously preparing orders at varying times defined by `prepTime`. Once an order is ready, it becomes available for pickup by a courier, which is where the Consumer comes into play. The Consumer handles the logistics of pairing ready orders with available couriers.

### **2. Efficient Handling of Concurrent Processes**
In a real-time system, multiple processes need to run concurrently—orders are being prepared, couriers are arriving, and orders need to be matched with couriers in real-time. The Producer/Consumer pattern, particularly when implemented with a `BlockingQueue` in Java, naturally supports this concurrent processing. The kitchen (Producer) can add prepared orders to the queue, while couriers (Consumers) can simultaneously take orders from the queue as they become ready.

### **3. Flexibility in Implementing Different Dispatch Strategies**
The problem requires the implementation of two different courier dispatch strategies: Matched and First-In-First-Out (FIFO). The Producer/Consumer pattern provides a flexible framework to implement these strategies. 
- **Matched Strategy**: This strategy can be implemented by dispatching a courier for a specific order as soon as it’s prepared. The courier then waits until the specific order is ready.
- **FIFO Strategy**: This strategy can be implemented by dispatching couriers to pick up the next available order, ensuring that orders are picked up in the order they are prepared.

### **4. Managing Synchronization and Resource Allocation**
The challenge also involves managing the synchronization between order preparation and courier arrival, particularly since couriers may arrive before the order is ready. The `BlockingQueue` used in the Producer/Consumer pattern helps manage this synchronization efficiently. If a courier arrives and there’s no order ready, it simply waits until an order becomes available in the queue, ensuring that no resources are wasted and that the system runs smoothly.

### **5. Real-Time Simulation**
Given the real-time nature of the simulation, the Producer/Consumer pattern allows for continuous and dynamic handling of orders and couriers without blocking the entire system. The kitchen can keep preparing new orders while couriers continue to arrive and pick up orders, mimicking the continuous operation of a real restaurant.

### **6. Scalability and Maintainability**
The Producer/Consumer pattern is inherently scalable and maintainable. As the system grows, such as handling more orders per second or adding more couriers, the design can easily adapt by tuning the number of Producers and Consumers. This makes it easier to maintain and extend the system as needed.

In summary, the Producer/Consumer design pattern provides a robust, flexible, and efficient way to handle the concurrent processes involved in the fulfillment of delivery orders in a kitchen, making it an ideal approach for this problem.

