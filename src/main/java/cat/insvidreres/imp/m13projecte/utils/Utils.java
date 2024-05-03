package cat.insvidreres.imp.m13projecte.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

public interface Utils {

    public String SALT = "social-post-salt-dam";

    enum CollectionName {
        USER("users"),
        POST("posts"),
        LIKE("likes"),
        COMMENT("comments"),
        CATEGORIES("categories"),;

        private final String TEXT;

        CollectionName(final String TEXT) {
            this.TEXT = TEXT;
        }

        @Override
        public String toString() {
            return TEXT;
        }
    }

    default JSONResponse generateResponse(int code, String date, String message, List<Object> data) {
        if (data == null) {
            return new JSONResponse(code, date, message);
        } else {
            return new JSONResponse(code, date, message, data);
        }
    }


    default String encryptPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        String pwWithSalt = password + ":" + salt;

        byte[] result = md.digest(pwWithSalt.getBytes(StandardCharsets.UTF_8));

        String encodedString = Base64.getEncoder().encodeToString(result);
        return encodedString;
    }

    default String testFirebaseHash(String password, String salt) {
        int rounds = 8;
        int mem_cost = 14;

        String finalPassword = password + ":" + salt;

        try {
            byte[] saltBytes = Base64.getDecoder().decode(SALT);
            KeySpec spec = new PBEKeySpec(finalPassword.toCharArray(), saltBytes, rounds, mem_cost);
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


    default JSONResponse checkIdToken(String idToken) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            if (token == null) {
                return generateResponse(
                        401,
                        LocalDateTime.now().toString(),
                        "User token not found!",
                        null
                );
            }
        } catch (FirebaseAuthException e) {
            System.out.println("Error getting token | " + e.getMessage());
            generateResponse(
                    500,
                    LocalDateTime.now().toString(),
                    "Error getting token | " + e.getMessage(),
                    null
            );
        }

        return null;
    }

}
