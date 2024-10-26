package Transport;

import java.net.DatagramPacket;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 25th , 2024
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
