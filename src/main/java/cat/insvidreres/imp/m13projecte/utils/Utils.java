package cat.insvidreres.imp.m13projecte.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public interface Utils {

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



    default String generateRandomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        return new String(salt, StandardCharsets.UTF_8);
    }

    default String encryptPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        String pwWithSalt = password + ":" + salt;

        byte[] result = md.digest(pwWithSalt.getBytes(StandardCharsets.UTF_8));

        return new String(result, StandardCharsets.UTF_8);
    }
}
