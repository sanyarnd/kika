package kika.security.jwt.encode;

import java.time.Clock;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshTokenGenerator {
    private static final char[] DEFAULT_CODEC =
        "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();

    private final Duration tokenLifetime;
    private final int tokenLength;
    private final Clock clock;
    private final Random random;

    public RefreshToken generate() {
        byte[] bytes = new byte[tokenLength];
        random.nextBytes(bytes);
        return new RefreshToken(bytesToString(bytes), clock.instant().plus(tokenLifetime));
    }

    @SuppressWarnings("MagicNumber")
    private String bytesToString(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            chars[i] = DEFAULT_CODEC[(bytes[i] & 0xFF) % DEFAULT_CODEC.length];
        }
        return new String(chars);
    }
}
