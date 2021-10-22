package kika.security.jwt.encode;

import java.time.Clock;
import kika.domain.Account;
import kika.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JwtTokenService {
    private final AccountRepository accountRepository;
    private final AccessTokenGenerator accessTokenGenerator;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final Clock clock;

    @Transactional
    public JwtToken generate(long id) {
        Account user = accountRepository.findById(id).orElseThrow();
        return generateInternal(user);
    }

    @Transactional
    public JwtToken refresh(String token) {
        Account user = accountRepository.findByRefreshToken(token).orElseThrow(); // TODO
        if (user.getRefreshTokenExpireAt().isBefore(clock.instant())) {
            throw new BadCredentialsException("Refresh token has expired");
        }
        return generateInternal(user);
    }

    private JwtToken generateInternal(Account user) {
        RefreshToken refreshToken = refreshTokenGenerator.generate();
        String accessToken = accessTokenGenerator.generate(user);

        user.setRefreshToken(refreshToken.token());
        user.setRefreshTokenExpireAt(refreshToken.expireAt());

        return new JwtToken(accessToken, refreshToken.token());
    }
}
