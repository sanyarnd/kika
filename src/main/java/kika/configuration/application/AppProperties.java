package kika.configuration.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "kika", ignoreUnknownFields = false)
public class AppProperties {
    private Jwt jwt;

    @Getter
    @RequiredArgsConstructor
    @ConstructorBinding
    public static class Jwt {
        private final AccessTokenProperties accessToken;
        private final CookieProperties cookie;
        private final RefreshTokenProperties refreshToken;
    }
}
