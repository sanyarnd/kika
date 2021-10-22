package kika.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kika.security.jwt.encode.JwtCookieService;
import kika.security.jwt.encode.JwtToken;
import kika.security.jwt.encode.JwtTokenService;
import kika.security.principal.KikaPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenService jwtTokenService;
    private final JwtCookieService jwtCookieService;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        KikaPrincipal principal = (KikaPrincipal) authentication.getPrincipal();
        JwtToken token = jwtTokenService.generate(principal.accountId());

        response.addCookie(jwtCookieService.accessCookie(token.accessToken()));
        response.addCookie(jwtCookieService.refreshCookie(token.refreshToken()));

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
