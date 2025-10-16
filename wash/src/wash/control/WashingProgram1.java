package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * <p>
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * <p>
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram1(WashingIO io,
                           ActorThread<WashingMessage> temp,
                           ActorThread<WashingMessage> water,
                           ActorThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    @Override
    public void run() {
        try {
            // Lock the hatch
            io.lock(true);

            //fill water
            System.out.println("setting WATER_FILL...");
            water.send(new WashingMessage(this, WATER_FILL));
            WashingMessage acktest = receive();
            System.out.println("washing program 1 got " + acktest);

            //drain water
            System.out.println("setting WATER_DRAIN...");
            water.send(new WashingMessage(this, WATER_DRAIN));
            WashingMessage acktest1 = receive();
            System.out.println("washing program 1 got " + acktest1);

            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, SPIN_SLOW));
            WashingMessage ack1 = receive();
            System.out.println("washing program 1 got " + ack1);

            // Spin for five simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);

            // Instruct SpinController to stop spin barrel spin.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, SPIN_OFF));
            WashingMessage ack2 = receive();
            System.out.println("washing program 1 got " + ack2);

            // Now that the barrel has stopped, it is safe to open the hatch.
            io.lock(false);

        } catch (InterruptedException e) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle

            temp.send(new WashingMessage(this, TEMP_IDLE));
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }
}

