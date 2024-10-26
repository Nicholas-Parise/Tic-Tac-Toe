package Transport;

import java.net.DatagramPacket;

/**
 * A simple class that will be held in the queue.
 */
public class SentPacket {

    DatagramPacket packet;
    long time;
    int sequence;

    public SentPacket(DatagramPacket packet, int sequence){
        this.packet = packet;
        this.sequence = sequence;
        this.time = System.currentTimeMillis();
    }

    public void resetTime(){
        this.time = System.currentTimeMillis();
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public int getSequence() {
        return sequence;
    }

    public long getTime() {
        return time;
    }

}
