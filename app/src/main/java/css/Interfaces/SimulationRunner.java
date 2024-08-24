package css.Interfaces;

/**
 * The SimulationRunner interface defines a contract for running a simulation.
 * Implementations of this interface are expected to encapsulate the logic 
 * required to execute the simulation process.
 */
public interface SimulationRunner {

    /**
     * Starts and executes the simulation. Implementations should contain all 
     * the necessary logic to initialize, run, and manage the simulation lifecycle.
     * 
     * This method handles any necessary setup before the simulation begins, 
     * the execution of the simulation itself, and any post-processing or cleanup 
     * required after the simulation has completed.
     */
    void run();
}
