package kika.security.jwt.decode;

import java.util.Collection;
import kika.security.principal.KikaPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthentication extends AbstractAuthenticationToken {
    private final KikaPrincipal principal;

    public JwtAuthentication(
        KikaPrincipal principal,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public KikaPrincipal getPrincipal() {
        return principal;
    }
}
