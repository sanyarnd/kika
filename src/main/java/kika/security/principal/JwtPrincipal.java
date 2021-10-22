package kika.security.principal;

import kika.domain.Account;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class JwtPrincipal implements KikaPrincipal {
    private final long accountId;
    private final Account.Provider provider;
    private final String providerId;

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
