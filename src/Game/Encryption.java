package Game;

import javax.crypto.*;
import javax.net.ssl.KeyManager;
import java.io.*;
import java.security.*;

public class Encryption {

    /**
     * @author Nicholas Parise
     * @version 1.0
     * @course COSC 4P14
     * @assignment #4
     * @student Id 7242530
     * @since Dec 11th , 2024
     */

    public static SecretKey generateAES(){
        try{
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
        }catch (NoSuchAlgorithmException e){}
        return null;
    }

    public static KeyPair generateRSA(){
       try {
           KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
           keyGen.initialize(2048);
           return keyGen.generateKeyPair();
       }catch (NoSuchAlgorithmException e){}
       return null;
    }

    public static byte[] encryptWithRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptWithRSA(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }



    /**
     * Turn object into array of bytes
     * These bytes are then send or reviewed
     * @param data
     * @return
     */
    public static byte[] encryptWithAES(GameData data, SecretKey key){
        try {
            ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
            ObjectOutputStream objStr = new ObjectOutputStream(byteStr);
            objStr.writeObject(data);
            objStr.flush();

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(byteStr.toByteArray());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * turn stream of bytes into an object
     * @param data
     * @return
     */
    public static GameData decryptWithAES(byte[] data, SecretKey key){
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedData = cipher.doFinal(data);

            ByteArrayInputStream byteStr = new ByteArrayInputStream(decryptedData);
            ObjectInputStream objStr = new ObjectInputStream(byteStr);
            GameData gd = (GameData) objStr.readObject();
            return gd;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
