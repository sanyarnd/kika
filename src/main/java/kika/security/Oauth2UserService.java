package kika.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import kika.domain.Account;
import kika.repository.AccountRepository;
import kika.security.principal.OAuth2Principal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Oauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Account.Provider provider = Account.Provider.valueOf(registrationId.toUpperCase());
        String providerId = extractOauth2Id(user, provider);

        Account account = accountRepository.findByProviderId(provider, providerId)
            .orElseGet(() -> accountRepository.save(new Account(user.getName(), provider, providerId)));

        return new OAuth2Principal(user, account.safeId(), provider, providerId);
    }

    @SuppressFBWarnings(
        value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
        justification = "Known bug: https://github.com/spotbugs/spotbugs/issues/651"
    )
    private String extractOauth2Id(OAuth2User user, Account.Provider provider) {
        return switch (provider) {
            case GITHUB -> Objects.requireNonNull(user.getAttribute("id")).toString();
            case GOOGLE -> throw new NotImplementedException("Google auth is not yet implemented");
        };
    }
}
