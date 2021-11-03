package kika.security.jwt.decode;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Based on {@link org.springframework.security.web.authentication.www.BasicAuthenticationFilter}
 */
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenAuthenticationConverter authenticationConverter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AuthenticationManager authenticationManager;

    @Override
    public void doFilterInternal(
        @NotNull HttpServletRequest request,
        @NotNull HttpServletResponse response,
        @NotNull FilterChain chain
    )
        throws ServletException, IOException {
        try {
            @Nullable JwtAuthenticationToken token = authenticationConverter.convert(request);
            if (token != null) {
                logger.debug("Found JWT token, token=" + token.getCredentials());

                if (authenticationIsRequired()) {
                    Authentication authentication = authenticationManager.authenticate(token);

                    logger.debug("Authentication success: " + authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            chain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            logger.debug("Authentication request failed!", ex);
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

    private boolean authenticationIsRequired() {
        @Nullable Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }
}
