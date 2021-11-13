package kika.security.jwt.encode;

import javax.servlet.http.Cookie;
import kika.configuration.application.CookieProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtCookieService {
    private final CookieProperties refreshCookieProperties;
    private final CookieProperties accessCookieProperties;
    private final String refreshTokenName;
    private final String accessTokenName;

    public Cookie refreshCookie(String refreshToken) {
        return createCookieInternal(refreshTokenName, refreshToken, refreshCookieProperties);
    }

    public Cookie accessCookie(String accessToken) {
        return createCookieInternal(accessTokenName, accessToken, accessCookieProperties);
    }

    private Cookie createCookieInternal(String cookieName, String cookieValue, CookieProperties cookieProperties) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(cookieProperties.path());
        cookie.setDomain(cookieProperties.domain());
        cookie.setSecure(cookieProperties.secure());
        cookie.setHttpOnly(cookieProperties.isHttpOnly());
        cookie.setMaxAge(Math.toIntExact(cookieProperties.maxAge().toSeconds()));

        return cookie;
    }
}
