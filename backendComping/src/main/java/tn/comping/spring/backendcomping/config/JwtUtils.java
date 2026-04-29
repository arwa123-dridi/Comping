package tn.comping.spring.backendcomping.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.entities.Role;

import java.util.concurrent.ConcurrentHashMap;
import java.security.Key;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtils {
    private final String jwtSecret="compingSecretKeyForJWTMustBe256BitsLongAtLeast!!";

    //token session duration in milliseconds (1 heure)
    private final long jwtExpirationMs = 3600000;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(String email,String id, Role role){

        return Jwts.builder()
                .setSubject(email)
                .claim("id", id)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime()+jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token){

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String getIdFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);
    }
    public boolean validateJwtToken(String token){

        try{
            if (blacklistedTokens.contains(token)) return false;
            
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
}
