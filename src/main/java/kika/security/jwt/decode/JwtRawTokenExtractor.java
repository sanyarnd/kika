package kika.security.jwt.decode;

import javax.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface JwtRawTokenExtractor {
    @Nullable String extract(HttpServletRequest request);
}
