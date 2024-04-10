package cat.insvidreres.imp.m13projecte.service;

import cat.insvidreres.imp.m13projecte.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JWTService {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 604_800_000; // 1 week

    static String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setIssuer("https://identitytoolkit.google.com/")
                .setAudience("social-post-m13")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("iss", "https://securetoken.google.com/social-post-m13")
                .claim("user_id", user.getEmail())
                .claim("email", user.getEmail())
                .claim("password", user.getPassword())
                .claim("verified", false)
                .claim("display_name", user.getFirstName())
                .signWith(SECRET_KEY)
                .compact();
    }
}