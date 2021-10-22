package kika.security.jwt.decode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import java.util.Collection;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * Extract JWT token from the request if possible.
 * <p>
 * Based on {@link org.springframework.security.web.authentication.www.BasicAuthenticationConverter}
 */
@RequiredArgsConstructor
public class JwtTokenAuthenticationConverter implements AuthenticationConverter {
    private final JwtParser jwtParser;
    private final Collection<JwtRawTokenExtractor> rawTokenExtractors;

    @Override
    public @Nullable JwtAuthenticationToken convert(HttpServletRequest request) {
        return rawTokenExtractors.stream()
            .map(extractor -> extractor.extract(request))
            .filter(Objects::nonNull)
            .findFirst()
            .map(this::convert)
            .map(JwtAuthenticationToken::new)
            .orElse(null);
    }

    private @Nullable Jwt<Header<?>, Claims> convert(String token) {
        try {
            Jwt<?, ?> jwtToken = jwtParser.parse(token);
            if (!(jwtToken.getBody() instanceof Claims)) {
                throw new BadCredentialsException("JWT token is missing 'claims' section");
            }

            return (Jwt<Header<?>, Claims>) jwtToken;
        } catch (ExpiredJwtException ex) {
            return null; // TODO: check route path?
        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid JWT token", ex);
        }
    }
}
