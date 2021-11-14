package kika.configuration.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
public record RefreshTokenProperties(Duration tokenLifetime, int tokenLength) {
}
