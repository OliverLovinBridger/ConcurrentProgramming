package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.io.WashingIO.Spin;
import wash.control.WashingMessage.Order;

public class SpinController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private Spin spin;
    private ActorThread<WashingMessage> sender;


    public SpinController(WashingIO io) {
        this.io = io;
        spin = Spin.IDLE;
    }

    @Override
    public void run() {

        // this is to demonstrate how to control the barrel spin:
        io.setSpinMode(Spin.IDLE);

        try {

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);

                    switch (m.order()) {
                        case SPIN_SLOW -> {
                            spin = Spin.RIGHT;

                        }
                        case SPIN_FAST -> {
                            spin = Spin.FAST;

                        }
                        case SPIN_OFF -> {
                            spin = Spin.IDLE;

                        }
                        default -> {
                        }
                    }
                }

                if (spin == Spin.LEFT) {
                    spin = Spin.RIGHT;

                } else if (spin == Spin.RIGHT) {
                    spin = Spin.LEFT;
                }

                io.setSpinMode(spin);

                if (m != null) {
                    m.sender().send(new WashingMessage(this, Order.ACKNOWLEDGMENT));
                }

            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}

