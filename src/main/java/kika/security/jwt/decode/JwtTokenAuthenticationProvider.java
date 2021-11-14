package kika.security.jwt.decode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import kika.domain.Account;
import kika.security.principal.JwtPrincipal;
import kika.security.principal.KikaPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAuthenticationProvider implements AuthenticationProvider {
    @Override
    @SuppressFBWarnings(
        value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
        justification = "Known bug: https://github.com/spotbugs/spotbugs/issues/651"
    )
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new IllegalArgumentException(
                "Authentication $authentication must be of type " + JwtAuthenticationToken.class.getSimpleName()
            );
        }

        long accountId = Long.parseLong(Objects.requireNonNull(jwtAuthenticationToken.subject()));
        Account.Provider provider = Objects.requireNonNull(jwtAuthenticationToken.provider());
        String providerId = Objects.requireNonNull(jwtAuthenticationToken.providerId());

        KikaPrincipal principal = new JwtPrincipal(accountId, provider, providerId);
        JwtAuthentication jwtAuthentication = new JwtAuthentication(principal, authentication.getAuthorities());
        jwtAuthentication.setDetails(jwtAuthenticationToken.getDetails());
        return jwtAuthentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
