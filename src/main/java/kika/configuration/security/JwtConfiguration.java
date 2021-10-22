package kika.configuration.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;
import kika.configuration.application.AccessTokenProperties;
import kika.configuration.application.AppProperties;
import kika.configuration.application.CookieProperties;
import kika.configuration.application.RefreshTokenProperties;
import kika.repository.AccountRepository;
import kika.security.jwt.decode.CookieJwtTokenExtractor;
import kika.security.jwt.decode.HeaderJwtTokenExtractor;
import kika.security.jwt.decode.JwtRawTokenExtractor;
import kika.security.jwt.decode.JwtTokenAuthenticationConverter;
import kika.security.jwt.decode.JwtTokenAuthenticationFilter;
import kika.security.jwt.encode.AccessTokenGenerator;
import kika.security.jwt.encode.JwtCookieService;
import kika.security.jwt.encode.JwtTokenService;
import kika.security.jwt.encode.RefreshTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class JwtConfiguration {
    @Bean
    HeaderJwtTokenExtractor headerJwtTokenExtractor() {
        return new HeaderJwtTokenExtractor(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME);
    }

    @Bean
    CookieJwtTokenExtractor cookieJwtTokenExtractor() {
        return new CookieJwtTokenExtractor(SecurityConfiguration.ACCESS_TOKEN_COOKIE_NAME);
    }

    @Bean
    JwtParser jwtParser(AppProperties appProperties) {
        Key privateKey = appProperties.getJwt().getAccessToken().getPrivateKey();
        return Jwts.parserBuilder().setSigningKey(privateKey).build();
    }

    @Bean
    JwtTokenAuthenticationConverter jwtTokenAuthenticationConverter(
        JwtParser jwtParser,
        CookieJwtTokenExtractor cookieJwtTokenExtractor,
        HeaderJwtTokenExtractor headerJwtTokenExtractor
    ) {
        List<JwtRawTokenExtractor> tokenExtractors = List.of(headerJwtTokenExtractor, cookieJwtTokenExtractor);
        return new JwtTokenAuthenticationConverter(jwtParser, tokenExtractors);
    }

    @Bean
    JwtCookieService jwtCookieService(AppProperties appProperties) {
        CookieProperties cookieProperties = appProperties.getJwt().getCookie();
        return new JwtCookieService(
            cookieProperties,
            cookieProperties,
            SecurityConfiguration.REFRESH_TOKEN_COOKIE_NAME,
            SecurityConfiguration.ACCESS_TOKEN_COOKIE_NAME
        );
    }

    @Bean
    AccessTokenGenerator accessTokenGenerator(AppProperties appProperties) {
        AccessTokenProperties props = appProperties.getJwt().getAccessToken();
        return new AccessTokenGenerator(props.getPrivateKey(), props.getExpirationTime());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RefreshTokenGenerator refreshTokenGenerator(AppProperties appProperties, Clock clock) {
        RefreshTokenProperties props = appProperties.getJwt().getRefreshToken();
        return new RefreshTokenGenerator(props.tokenLifetime(), props.tokenLength(), clock, new SecureRandom());
    }

    @Bean
    JwtTokenService jwtTokenService(
        AccountRepository accountRepository,
        AccessTokenGenerator accessTokenGenerator,
        RefreshTokenGenerator refreshTokenGenerator,
        Clock clock
    ) {
        return new JwtTokenService(accountRepository, accessTokenGenerator, refreshTokenGenerator, clock);
    }

    @Bean
    JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter(
        JwtTokenAuthenticationConverter jwtTokenAuthenticationConverter,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        return new JwtTokenAuthenticationFilter(
            jwtTokenAuthenticationConverter, authenticationEntryPoint);
    }
}
