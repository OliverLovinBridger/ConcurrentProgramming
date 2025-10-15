package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

        WashingIO io = sim.startSimulation();

        ActorThread<WashingMessage> temp = new TemperatureController(io);
        ActorThread<WashingMessage> water = new WaterController(io);
        ActorThread<WashingMessage> spin = new SpinController(io);

        temp.start();
        water.start();
        spin.start();

        ActorThread<WashingMessage> currentProgram = null;

        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);

            switch (n) {
                case 0 -> {
                    if(currentProgram != null) {
                        System.out.println("Forced stop");
                        currentProgram.interrupt();
                        currentProgram = null;
                    }
                }
                case 1 -> {}
                case 2 -> {}
                case 3 -> {
                    currentProgram = new WashingProgram3(io, temp, water, spin);
                    currentProgram.start();
                }
            }
            // if the user presses buttons 1-3, start a washing program
            // if the user presses button 0, and a program has been started, stop it
        }
    }
};
