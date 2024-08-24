package css.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the statistics for each simulation. 
 * Keeps track and calculates the statisitcs for
 * the food and courier wait times.
 */
public class Statistics {
    
    private List<Long> foodWaitTimes;
    private List<Long> courierWaitTimes;

    public Statistics(){
        foodWaitTimes = new ArrayList<>();
        courierWaitTimes = new ArrayList<>();
    }

    public List<Long> getFoodWaitTimes() {
        return foodWaitTimes;
    }

    public List<Long> getCourierWaitTimes() {
        return courierWaitTimes;
    }

    public long calculateAverage(List<Long> waitTimes) {
        if (waitTimes == null || waitTimes.isEmpty()) {
            System.out.println("List is not valid.");
            return 0;
        }
        long sum = 0;
        for (Long time : waitTimes) {
            sum += time;
        }
        return sum / waitTimes.size();
    }

    // Print statistics for one order being fulfilled.
    public void printFulFillmentStats(Courier courier, Order order, long endWaitingTime) {
        long foodWaitTime = endWaitingTime - order.getFinishedTime().toEpochMilli();
        long courierWaitTime = endWaitingTime - courier.getArrivalTime().toEpochMilli();

        foodWaitTimes.add(foodWaitTime);
        courierWaitTimes.add(courierWaitTime);

        // Batch logging to minimize I/O overhead
        String logMessage = String.format("Matched: Order %s picked up by Courier %s%n" +
                                          "Order %s delivered by Courier %s%n" +
                                          "Food Wait Time: %d ms%n" +
                                          "Courier Wait Time: %d ms%n",
                                          order.getId(), courier.getId(),
                                          order.getId(), courier.getId(),
                                          foodWaitTime, courierWaitTime);
        System.out.println();
        System.out.print(logMessage);
        System.out.flush();
    }

    // Print statisitcs for the whole simulation
    public void printStatistics() {
        long avgFoodWaitTimeInMilliseconds = calculateAverage(foodWaitTimes);
        long avgCourierWaitTimeInMilliseconds = calculateAverage(courierWaitTimes);
        System.out.println("\nThe Average Food Wait time is " + avgFoodWaitTimeInMilliseconds
                + " ms Average Courier Wait time is " + avgCourierWaitTimeInMilliseconds + " ms");
        System.out.println("");
    }
}
