package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.control.WashingMessage.Order;

public class TemperatureController extends ActorThread<WashingMessage> {


    private WashingIO io;
    ActorThread<WashingMessage> sender;
    State state;
    private enum State{WASHING_40, WASHING_60, COOLING, IDLE}
    private int dt = 10;
    private boolean ack = false;

    public TemperatureController(WashingIO io) {
        this.io = io;
        state = State.IDLE;
    }

    @Override
    public void run() {

        try {

            while (true) {
                WashingMessage m = receiveWithTimeout(dt * 1000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);
                    ack = true;
                    sender = m.sender();
                    switch(m.order()) {
                        case TEMP_SET_40 -> {state = State.WASHING_40;}
                        case TEMP_SET_60 -> {state = State.WASHING_60;}
                        case TEMP_IDLE -> {state = State.IDLE;}
                    }
                }
                double upperLimit;
                double lowerLimit;
                double currentTemperature;
                double mu;
                double ml;

                switch (state) {
                    case WASHING_40 -> {
                        upperLimit = 40.0;
                        lowerLimit = 38.0;
                        currentTemperature = io.getTemperature();

                        mu = 0.0478 * dt;
                        ml = 9.52 * dt * Math.pow(10, -3);

                        if(currentTemperature <= lowerLimit + ml) {
                            io.heat(true);
                        } else if(currentTemperature >= upperLimit -mu) {
                            io.heat(false);
                            if (ack) {
                                sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                                ack = false;
                            }
                        }

                    }
                    case WASHING_60 -> {
                        upperLimit = 60.0;
                        lowerLimit = 58.0;
                        currentTemperature = io.getTemperature();

                        mu = 0.0478 * dt;
                        ml = 9.52 * dt * Math.pow(10, -3);

                        if(currentTemperature <= lowerLimit + ml) {
                            io.heat(true);
                        } else if(currentTemperature >= upperLimit - mu) {
                            io.heat(false);
                            if (ack) {
                                sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                                ack = false;
                            }
                        }
                    }
                    case IDLE -> {
                        io.heat(false);
                        if(ack) {
                            sender.send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                            ack = false;
                        }
                    }
                    default -> {}
                }


            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}


