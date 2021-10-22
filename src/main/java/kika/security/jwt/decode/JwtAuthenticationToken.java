package kika.security.jwt.decode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import kika.domain.Account;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Transient;

@Transient
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Jwt<Header<?>, Claims> token;

    JwtAuthenticationToken(Jwt<Header<?>, Claims> token) {
        super(null);
        this.token = token;
    }

    public Map<String, Object> claims() {
        return token.getBody();
    }

    public @Nullable String id() {
        return token.getBody().getId();
    }

    public @Nullable String issuer() {
        return token.getBody().getIssuer();
    }

    public @Nullable String audience() {
        return token.getBody().getAudience();
    }

    public @Nullable String subject() {
        return token.getBody().getSubject();
    }

    public @Nullable Instant expiration() {
        Date expiration = token.getBody().getExpiration();
        return expiration != null ? expiration.toInstant() : null;
    }

    public @Nullable Instant notBefore() {
        Date notBefore = token.getBody().getNotBefore();
        return notBefore != null ? notBefore.toInstant() : null;
    }

    public @Nullable Instant issuedAt() {
        Date issuedAt = token.getBody().getIssuedAt();
        return issuedAt != null ? issuedAt.toInstant() : null;
    }

    public @Nullable Account.Provider provider() {
        return Account.Provider.valueOf((String) claims().get("provider"));
    }

    public @Nullable String providerId() {
        return String.valueOf(claims().get("providerId"));
    }

    @Override
    public Jwt<Header<?>, Claims> getCredentials() {
        return token;
    }

    @Override
    public @Nullable String getPrincipal() {
        return token.getBody().getSubject();
    }

    @Override
    public @Nullable String getName() {
        return getPrincipal();
    }

    @Override
    public String toString() {
        return "JwtAuthenticationToken[id=%s, subject=%s, expiresAt=%s]".formatted(id(), subject(), expiration());
    }
}
