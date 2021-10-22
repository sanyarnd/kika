package kika.security.jwt.decode;

import java.util.Arrays;
import java.util.Objects;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class CookieJwtTokenExtractor implements JwtRawTokenExtractor {
    private final String cookieName;

    @Override
    public @Nullable String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> Objects.equals(cookie.getName(), cookieName))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
    }
}
