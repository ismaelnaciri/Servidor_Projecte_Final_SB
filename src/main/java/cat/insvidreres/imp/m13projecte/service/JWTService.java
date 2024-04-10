package cat.insvidreres.imp.m13projecte.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JWTService {

    private static final String SECRET_KEY = "social_post_projecte_final";
    private static final long EXPIRATION_TIME = 604_800_000; // 1 week

    public String generateToken(String userId, String email, String displayName, boolean verified) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setIssuer("https://identitytoolkit.google.com/")
                .setAudience("social-post-m13")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("user_id", userId)
                .claim("email", email)
                .claim("verified", false)
                .claim("display_name", displayName)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}
