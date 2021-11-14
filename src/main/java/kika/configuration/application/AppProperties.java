package kika.configuration.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "kika", ignoreUnknownFields = false)
public class AppProperties {
    @NestedConfigurationProperty
    private JwtProperties jwt;
    private String authRedirectUrl;

    @Getter
    @AllArgsConstructor
    @ConstructorBinding
    public static class JwtProperties {
        @NestedConfigurationProperty
        private AccessTokenProperties accessToken;
        @NestedConfigurationProperty
        private CookieProperties cookie;
        @NestedConfigurationProperty
        private RefreshTokenProperties refreshToken;
    }
}
