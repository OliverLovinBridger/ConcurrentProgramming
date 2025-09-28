package lift;

public class LiftMain {
    public static void main(String[] args) {
        final int NBR_FLOORS =7, MAX_PASSENGERS = 4, NBR_PASSENGERS = 20; //X1, but changhing these values.
        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        LiftMonitor monitor = new LiftMonitor(NBR_FLOORS, MAX_PASSENGERS);

        for (int i = 0; i < NBR_PASSENGERS; i++) {
            new PassengerThread(monitor, view).start();
        }

        new LiftThread(monitor, view, NBR_FLOORS).start();

    }
}
