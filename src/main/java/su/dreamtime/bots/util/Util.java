package su.dreamtime.bots.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Util {
    private static Random random = new Random();
    public static String generateRandomHash() {
        return generateRandomHash(20);
    }
    public static String generateRandomHash(int length) {
        return md5(rndString(length));
    }
    public static String rndString(int length)
    {
        return rndString(length, false);
    }
    public static String rndString(int length, boolean withSymbols)
    {
        String generateFrom = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (withSymbols) {
            generateFrom += "!@#$%^&*()_-+=";
        }
        StringBuilder rndString = new StringBuilder(length);

        for (int i = 0; i < length; i++)
        {
            int rndInt = random.nextInt(generateFrom.length());

            rndString.append(generateFrom.charAt(rndInt));
        }

        return rndString.toString();
    }

    public static String md5(String str)
    {
        String md5string = null;
        try
        {
            byte[] bytes = str.getBytes("UTF-8");
            byte[] md5 = MessageDigest.getInstance("MD5").digest(bytes);
            BigInteger bigInt = new BigInteger(1, md5);

            md5string  = bigInt.toString(16);
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return md5string;
    }

    public static Random getRandom() {
        return random;
    }
}
