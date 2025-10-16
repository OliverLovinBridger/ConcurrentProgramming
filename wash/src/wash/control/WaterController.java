
package wash.control;
import actor.ActorThread;
import wash.io.WashingIO;
import wash.control.WashingMessage.Order;

public class WaterController extends ActorThread<WashingMessage> {

    private WashingIO io;
    ActorThread<WashingMessage> sender;
    private State state;
    private enum State{FILLING, DRAIN, IDLE}

    public WaterController(WashingIO io) {
        this.io = io;
        this.state = State.IDLE;
    }
    @Override
    public void run() {
        try {
            while (true) {
                WashingMessage m = receiveWithTimeout(1000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);
                    sender = m.sender();

                    switch(m.order()) {
                        case WATER_FILL -> {state = State.FILLING;}
                        case WATER_DRAIN -> {state = State.DRAIN;}
                        case WATER_IDLE -> {
                            io.drain(false);
                            state = State.IDLE;
                            sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                        }
                        default -> {}
                    }
                }

                switch (state) {
                    case FILLING -> {
                        io.drain(false);
                        if(io.getWaterLevel() < 10){
                            io.fill(true);
                        } else {
                            io.fill(false);
                            sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                            state = State.IDLE;
                        }
                    }
                    case DRAIN -> {
                        if(io.getWaterLevel() > 0){
                            io.drain(true);
                        } else {
                            sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                            state = State.IDLE;

                        }
                    }
                }
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}