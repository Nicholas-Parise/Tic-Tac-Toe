package Game;

import javax.crypto.*;
import java.io.*;
import java.security.*;

public class Encryption {


    public static KeyPair generateRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
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
    public static byte[] encryptWithAES(GameData data, SecretKey key) throws Exception{

        ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
        ObjectOutputStream objStr = new ObjectOutputStream(byteStr);
        objStr.writeObject(data);
        objStr.flush();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return cipher.doFinal(byteStr.toByteArray());
    }

    /**
     * turn stream of bytes into an object
     * @param data
     * @return
     */
    public static GameData decryptWithAES(byte[] data, SecretKey key) throws Exception{

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte[] decryptedData = cipher.doFinal(data);

        ByteArrayInputStream byteStr = new ByteArrayInputStream(decryptedData);
        ObjectInputStream objStr = new ObjectInputStream(byteStr);
        GameData gd = (GameData) objStr.readObject();
        return gd;
    }

}
