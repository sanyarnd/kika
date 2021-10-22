package kika.security.jwt.encode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kika.domain.Account;
import lombok.RequiredArgsConstructor;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
public class AccessTokenGenerator {
    private final Key privateKey;
    private final Duration expirationTime;

    String generate(Account user) {
        Claims claims = Jwts.claims();
//        claims.setId("kika-token");
        claims.setIssuer("kika");
        claims.setSubject(String.valueOf(user.safeId()));
        claims.put("provider", user.getProvider().toString());
        claims.put("providerId", user.getProviderId());
//        claims.setAudience("https://kika.com");

        claims.setIssuedAt(Date.from(Instant.now()));
        claims.setExpiration(Date.from(Instant.now().plus(expirationTime)));

        return Jwts.builder().signWith(privateKey).setClaims(claims).compact();
    }
}
