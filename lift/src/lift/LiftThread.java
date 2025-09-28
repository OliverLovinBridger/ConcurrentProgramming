package lift;

public class LiftThread extends Thread {
    private final LiftMonitor monitor;
    private final LiftView view;
    private final int nbrFloors;

    private int currentFloor;
    private int direction = 1;

    public LiftThread(LiftMonitor monitor, LiftView view, int nbrFloors) {
        this.monitor = monitor;
        this.view = view;
        this.nbrFloors = nbrFloors;
    }

    @Override
    public void run() {
        while (true) {
            //if there are no passengers inside or outside, the lift halts
            synchronized (monitor) {
                while (monitor.shouldHalt()) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            //Move lift to next floor, if we are at the top go down, if we are at the bottom go up
            int nextFloor = currentFloor + direction;
            if (nextFloor >= nbrFloors) {
                direction = -1;
                nextFloor = nbrFloors - 2;
            } else if (nextFloor < 0) {
                direction = 1;
                nextFloor = 1;
            }
            //Acutally moving the lift, also outside monitor as indicated by "(Timeconsuming operations should not be executed within the monitor.)"
            view.moveLift(currentFloor, nextFloor);
            currentFloor = nextFloor;

            monitor.liftArrivesAtFloor(currentFloor);


            //Open doors if needed, wait until passenger are done and close them.
            if (monitor.shouldDoorsOpen()) {
                view.openDoors(currentFloor);
                try {
                    monitor.openDoorsAtFloor();   // blocks until all passengers done
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                view.closeDoors();
            }
        }
    }
}
