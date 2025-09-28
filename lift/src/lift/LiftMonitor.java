package lift;

public class LiftMonitor {

    //Initialize
    private int nbrFloors;
    private int maxPassengers;

    //States
    private int currentFloor = 0;
    private int passengerCount = 0;
    private boolean doorsOpen = false;

    //Passengers wanting to enter or exit each floor
    private int[] toEnter;
    private int[] toExit;

    //I added these so that "Modify your solution to allow multiple passengers to enter and/or exit concurrently." Before that I simply had a different solution that worked for one passenger at time. Then I also didnt have openDoorsAtfloorsmethod but did those commands directly in finishedentering and finishedexiting.
    private int enteringCount = 0;
    private int exitingCount = 0;

    public LiftMonitor(int nbrFloors, int maxPassengers) {
        this.nbrFloors = nbrFloors;
        this.maxPassengers = maxPassengers;
        this.toEnter = new int[nbrFloors];
        this.toExit = new int[nbrFloors];
    }

    //Passenger arrives
    public synchronized void passengerArrives(int startFloor) {
        toEnter[startFloor]++;
        notifyAll();
    }

    //Passenger waiting to enter checking correct floor, doors open and passengercount.
    public synchronized void waitForEnter(int startFloor) throws InterruptedException {
        while (!(doorsOpen && startFloor == currentFloor && passengerCount < maxPassengers)) {
            wait();
        }
        enteringCount++;
        toEnter[startFloor]--;
        passengerCount++;
        notifyAll();
    }

    //Indicates to lift what floor to leave on
    public synchronized void finishedEntering(int destinationFloor) {
        toExit[destinationFloor]++;
        enteringCount--;
        notifyAll();
    }

    //wait for it to be safe to leave, then gives passenger permission to leave as long as there are som passenger there.
    public synchronized void waitForExit(int destinationFloor) throws InterruptedException {
        while (!(doorsOpen && destinationFloor == currentFloor && toExit[destinationFloor] > 0)) {
            wait();
        }
        exitingCount++;
        toExit[destinationFloor]--;
        notifyAll();
    }

    //Simple signaling that the passenger has left
    public synchronized void finishedExiting() {
        exitingCount--;
        passengerCount--;
        notifyAll();
    }

    //Obvious what this does hehe
    public synchronized void liftArrivesAtFloor(int floor) {
        currentFloor = floor;
        notifyAll();
    }

    //Checks whether anyone wants to enter, or if there is anyone in the lift leaving at said floor. // previous only like this "return toEnter[currentFloor] > 0 || toExit[currentFloor] > 0;" but wanted the lift to not stop if full and no one is exiting
    public synchronized boolean shouldDoorsOpen() {
        return (passengerCount < maxPassengers) && (toEnter[currentFloor] > 0) || (toExit[currentFloor] > 0);
    }

    //Initially check whether the lift should be moving at all
    public synchronized boolean shouldHalt() {
        return (passengerCount == 0 && totalWaitingOutside() == 0);
    }

    private int totalWaitingOutside() {
        int sum = 0;
        for (int i : toEnter) {
            sum += i;
        }
        return sum;
    }

    public synchronized void openDoorsAtFloor() throws InterruptedException {
        doorsOpen = true;
        notifyAll();

        while ((toEnter[currentFloor] > 0 && passengerCount < maxPassengers) // I had a deadlock when passengers were 4 and had to add more "villkor" here. Deadlock arose because i didnt distinguish between enteringcount(passengers actually entering) and (toEnter[currentfloor]) that is passengers waiting to enter.
                || toExit[currentFloor] > 0
                || enteringCount > 0
                || exitingCount > 0) {
            wait();
        }

        doorsOpen = false;
        notifyAll();
    }

}
