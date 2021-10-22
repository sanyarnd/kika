package kika.security.principal;

import java.util.Collection;
import java.util.Map;
import kika.domain.Account;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public final class OAuth2Principal implements OAuth2User, KikaPrincipal {
    private final OAuth2User oauth2User;
    private final long accountId;
    private final Account.Provider provider;
    private final String providerId;

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }

    @Override
    public long accountId() {
        return accountId;
    }

    @Override
    public @NotNull Account.Provider provider() {
        return provider;
    }

    @Override
    public @NotNull String providerId() {
        return providerId;
    }
}

