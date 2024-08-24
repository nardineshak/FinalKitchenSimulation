package css;

// import css.SimulationRunner.FIFOSimulationRunner;
import css.SimulationRunner.MatchedSimulationRunner;

public class Simulator {

    private static final String FILE_PATH = "dispatch_orders.json";
    
    public static void main(String[] args) {
        //Simulation 1: Matched Strategy
        System.out.println("Starting Matched Simulation...");
        MatchedSimulationRunner matchedSimulationRunner = new MatchedSimulationRunner(FILE_PATH, 10);
        matchedSimulationRunner.run();
        System.out.println();


        // //Simulation 2: FIFO Strategy
        // System.out.println("Starting FIFO Simulation...");
        // FIFOSimulationRunner fifoSimulationRunner = new FIFOSimulationRunner(FILE_PATH, 10);
        // fifoSimulationRunner.run();
        
        System.exit(0);
    }
}