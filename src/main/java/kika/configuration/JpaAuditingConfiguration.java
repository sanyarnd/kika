package kika.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
class JpaAuditingConfiguration {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return Optional.of("SYSTEM");
            } else {
                Object principal = auth.getPrincipal();
                if (principal instanceof String) {
//                if (principal instanceof Authentication) {
                    return Optional.of(principal.toString());
                } else {
                    throw new IllegalStateException(String.format("Unknown principal %s", principal.toString()));
                }
            }
        };
    }
}
