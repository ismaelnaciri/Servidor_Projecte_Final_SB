package cat.insvidreres.imp.m13projecte.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public interface Utils {

    public String SALT = "social-post-salt-dam";

    enum CollectionName {
        USER("users"),
        POST("posts"),
        LIKE("likes"),
        COMMENT("comments");

        private final String TEXT;

        CollectionName(final String TEXT) {
            this.TEXT = TEXT;
        }

        @Override
        public String toString() {
            return TEXT;
        }
    }


    default String encryptPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        String pwWithSalt = password + ":" + salt;

        byte[] result = md.digest(pwWithSalt.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(result);
    }

    default String testFirebaseHash(String password) {
        int rounds = 8;
        int mem_cost = 14;

        try {
            byte[] saltBytes = Base64.getDecoder().decode(SALT);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, rounds, mem_cost);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("SCRYPT");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    default String decodePassword(String password) {
        byte[] decodedBytes = Base64.getDecoder().decode(password);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
