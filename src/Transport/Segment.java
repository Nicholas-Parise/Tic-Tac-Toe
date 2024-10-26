package Transport;

import Game.GameData;

import java.io.*;

/**
 * @author Nicholas Parise
 * @version 1.0
 * @course COSC 4P14
 * @assignment #2
 * @student Id 7242530
 * @since Oct 25th , 2024
 */

public class Segment implements Serializable {

    private static final long serialVersionUID = 1L;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private GameData gameData;
    private MessageType messageType;

    public Segment(MessageType messageType,int sequenceNumber, int acknowledgmentNumber, GameData gameData){
        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.gameData = gameData;
        this.messageType = messageType;
    }

    public GameData getGameData() {
        return gameData;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public MessageType getMessageType() {
        return messageType;
    }


    /**
     * Turn object into array of bytes
     * These bytes are then send or reviewed
     * @param data
     * @return
     */
    public static byte[] serialize(Segment data){
        try{
            ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
            ObjectOutputStream objStr = new ObjectOutputStream(byteStr);
            objStr.writeObject(data);
            objStr.flush();
            return byteStr.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * turn stream of bytes into an object
     * @param data
     * @return
     */
    public static Segment deSerialize(byte[] data){
        try {
            ByteArrayInputStream byteStr = new ByteArrayInputStream(data);
            ObjectInputStream objStr = new ObjectInputStream(byteStr);
            Segment gd = (Segment) objStr.readObject();
            return gd;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



}
