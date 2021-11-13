package kika.controller;

import javax.servlet.http.HttpServletResponse;
import kika.configuration.security.SecurityConfiguration;
import kika.security.jwt.encode.JwtCookieService;
import kika.security.jwt.encode.JwtToken;
import kika.security.jwt.encode.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
class JwtController {
    private final JwtTokenService jwtTokenService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/login/refresh")
    public void refresh(
        @RequestHeader(SecurityConfiguration.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
        HttpServletResponse response
    ) {
        JwtToken token = jwtTokenService.refresh(refreshToken);
        response.addCookie(jwtCookieService.accessCookie(token.accessToken()));
        response.addCookie(jwtCookieService.refreshCookie(token.refreshToken()));
    }
}
