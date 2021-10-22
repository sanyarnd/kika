package kika.security.principal;

import kika.domain.Account;
import org.jetbrains.annotations.NotNull;

public sealed interface KikaPrincipal permits OAuth2Principal, JwtPrincipal {
    long accountId();

    @NotNull Account.Provider provider();

    @NotNull String providerId();
}
