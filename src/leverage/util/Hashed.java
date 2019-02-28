package leverage.util;

import leverage.exceptions.HashGenerationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Hashed {

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuffer.toString();
    }

    private static String hashFile(File file, String algorithm) throws HashGenerationException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new HashGenerationException("Could not generate hash from file");
        }
    }

    public static String generateMD5(File file) {
        String response = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");
        try {
            response = hashFile(file, "MD5");
        } catch (HashGenerationException e) {
            System.out.println('[' + dateFormat.format(new Date()) + "] Archivo: \"" + file.getName() + "\" en \"" + file.getPath() + "\" es un Directorio. Eliminelo por favor.");
        }
        return response;
    }

    public static String generateSHA1(File file) {
        String response = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");
        try {
            response = hashFile(file, "SHA-1");
        } catch (HashGenerationException e) {
            System.out.println('[' + dateFormat.format(new Date()) + "] Archivo: \"" + file.getName() + "\" en \"" + file.getPath() + "\" es un Directorio. Eliminelo por favor.");
        }
        return response;
    }

    public static String generateSHA256(File file) {
        String response = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");
        try {
            response = hashFile(file, "SHA-256");
        } catch (HashGenerationException e) {
            System.out.println('[' + dateFormat.format(new Date()) + "] Archivo: \"" + file.getName() + "\" en \"" + file.getPath() + "\" es un Directorio. Eliminelo por favor.");
        }
        return response;
    }
}
