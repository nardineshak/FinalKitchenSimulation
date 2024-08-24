package css;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import css.SimulationRunner.FIFOSimulationRunner;
import css.SimulationRunner.MatchedSimulationRunner;

public class SimulatorTest {

    @Test
    public void testSimulatorMainExecution() {
        try {
            // Run the main method of Simulator
            Simulator.main(new String[]{});
        } catch (Exception e) {
            // Fail the test if any exception occurs
            e.printStackTrace();
            assert false : "Exception occurred during Simulator execution: " + e.getMessage();
        }
    }

    @Test
    public void testFIFOSimulationRunnerInitialization() {
        // Initialize the FIFOSimulationRunner
        FIFOSimulationRunner fifoSimulationRunner = new FIFOSimulationRunner("dispatch_orders.json", 10);

        // Verify that the FIFOSimulationRunner object was created and not null
        assertNotNull(fifoSimulationRunner, "FIFOSimulationRunner should be initialized");
    }

    @Test
    public void testFIFOSimulationRunnerRun() {
        // Initialize the FIFOSimulationRunner
        FIFOSimulationRunner fifoSimulationRunner = new FIFOSimulationRunner("dispatch_orders.json", 10);

        // Run the simulation to ensure no exceptions are thrown
        try {
            fifoSimulationRunner.run();
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Exception occurred during FIFO simulation: " + e.getMessage();
        }
    }


    @Test
    public void testMatchedSimulationRunnerInitialization() {
        // Initialize the FIFOSimulationRunner
        MatchedSimulationRunner matchedSimulationRunner = new MatchedSimulationRunner("dispatch_orders.json", 10);

        // Verify that the FIFOSimulationRunner object was created and not null
        assertNotNull(matchedSimulationRunner, "FIFOSimulationRunner should be initialized");
    }

    @Test
    public void testMatchedSimulationRunnerRun() {
        // Initialize the FIFOSimulationRunner
        MatchedSimulationRunner matchedSimulationRunner = new MatchedSimulationRunner("dispatch_orders.json", 10);

        // Run the simulation to ensure no exceptions are thrown
        try {
            matchedSimulationRunner.run();
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "Exception occurred during FIFO simulation: " + e.getMessage();
        }
    }
}
