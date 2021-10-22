package kika.configuration.application;

import java.time.Duration;

public record RefreshTokenProperties(Duration tokenLifetime, int tokenLength) {
}
