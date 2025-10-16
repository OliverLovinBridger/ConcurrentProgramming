package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    // TODO: add attributes

    public TemperatureController(WashingIO io) {
        // TODO
    }

    @Override
    public void run() {

        try {

            while (true) {
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {}


            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}


