package css.Interfaces;

/**
 * The OrderProcessor interface defines the essential methods required for 
 * processing orders within a system. Implementations of this interface 
 * should handle the consumption, shutdown, and finalization of order 
 * processing. This interface extends Runnable, allowing for concurrent 
 * processing in a multi-threaded environment.
 */
public interface OrderProcessor extends Runnable {

    /**
     * Starts the process of consuming and processing orders from a data source.
     * This method is intended to run continuously, typically in a loop, 
     * until a shutdown is initiated.
     *
     * Implementations ensure thread-safety, when accessing the
     * shared resources.
     */
    void consume();

    /**
     * Initiates a controlled and graceful shutdown of the order processor. 
     * Once called, the system should stop accepting new orders and focus on 
     * completing any currently processing orders. This method should ensure 
     * that the system transitions to a safe state without data loss or corruption.
     */
    void shutdown();

    /**
     * Finalizes the processing after a shutdown has been initiated. This method 
     * should handle any remaining tasks that need to be completed before the 
     * processor is fully stopped, such as persisting the state, logging, or 
     * notifying other system components.
     */
    void finalizeProcessing();
}
