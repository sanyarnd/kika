package kika.configuration;

import java.util.Optional;
import kika.security.principal.KikaPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
class JpaAuditingConfiguration {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return Optional.of("-1");
            } else {
                Object principal = auth.getPrincipal();
                if (principal instanceof KikaPrincipal kikaPrincipal) {
                    return Optional.of(String.valueOf(kikaPrincipal.accountId()));
                } else {
                    throw new IllegalStateException(String.format("Unknown principal %s", principal.toString()));
                }
            }
        };
    }
}
