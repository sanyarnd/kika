package kika.security.jwt.decode;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class HeaderJwtTokenExtractor implements JwtRawTokenExtractor {
    private final String headerName;

    @Override
    public @Nullable String extract(HttpServletRequest request) {
        return request.getHeader(headerName);
    }
}
