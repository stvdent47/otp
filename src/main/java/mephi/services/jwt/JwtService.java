package mephi.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24; // 1 day

    public static String createJwt(String userId, String[] roles) {
        Instant now = Instant.now();
        Instant expDate = now.plus(EXPIRATION_TIME_MS, ChronoUnit.MILLIS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expDate))
            .signWith(SECRET_KEY)
            .compact();
    }

    public static Claims validateJwt(String jwt) {
        try {
            Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(jwt);

            return jwsClaims.getBody();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
