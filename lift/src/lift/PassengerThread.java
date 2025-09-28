package lift;

public class PassengerThread extends Thread {

    private final LiftMonitor monitor;
    private final LiftView view;

    public PassengerThread(LiftMonitor monitor, LiftView view) {
        this.monitor = monitor;
        this.view = view;
    }

    @Override
    public void run() {
        while (true) {
            //initialize/create passenger and begin.
            Passenger p = view.createPassenger();
            int startFloor = p.getStartFloor();
            int destinationFloor = p.getDestinationFloor();

            p.begin();
            //indicated to monitor that we have a passenger waiting at the specific floor
            monitor.passengerArrives(startFloor);
            //we are then waiting until the lift arrives and the doors open
            try {
                monitor.waitForEnter(startFloor);
            } catch (InterruptedException e) {
                return;
            }
            //Once that happens the passenger enters the lift
            p.enterLift();
            //We indicate to the monitor that the passenger has finished entering and what floor the passenger is going.
            monitor.finishedEntering(destinationFloor);
            //Then we wait until the lift arrives.
            try {
                monitor.waitForExit(destinationFloor);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //the passenger leaves
            p.exitLift();
            //we indicate to the montior that the passenger have left.
            monitor.finishedExiting();
            p.end();
        }
    }

}
