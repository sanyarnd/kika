package kika.security.jwt.encode;

import java.time.Instant;

public record RefreshToken(String token, Instant expireAt) {
}
