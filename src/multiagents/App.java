package multiagents;

import java.io.IOException;
import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.ManagementException;
import massim.javaagents.AgentsInterpreter;

/**
 * This app instantiates an interpreter (loading agents), creates the connection
 * to the MASSim-server, and executes the agents.
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("PHASE 1: INSTANTIATING INTERPRETER");
        AgentsInterpreter interpreter;
        if (args.length != 0) {
            interpreter = new AgentsInterpreter(args[0]);
        } else {
            interpreter = new AgentsInterpreter();
        }
        System.out.println("interpreter loaded");

        // load the interface
        System.out.println("");
        System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
        

        try {
            EnvironmentInterfaceStandard ei = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
            System.out.println("environment-interface loaded");

            try {
                ei.start();
            } catch (ManagementException e) {
                System.err.println(e.getMessage());
            }
            System.out.println("environment-interface started");

            System.out.println("");
            System.out.println("PHASE 3: CONNECTING INTERPRETER AND ENVIRONMENT");
            //  connect to environment
            interpreter.addEnvironment(ei);
            System.out.println("interpreter and environment connected");

            //  run stepwise
            System.out.println("");
            System.out.println("PHASE 4: RUNNING");
            int step = 1;
            boolean running = true;
            while (running) {
                System.out.println("STEP " + step);
                interpreter.step();
                step++;
                System.out.println("");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
