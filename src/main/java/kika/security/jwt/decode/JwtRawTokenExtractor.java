package kika.security.jwt.decode;

import org.jetbrains.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface JwtRawTokenExtractor {
    @Nullable String extract(HttpServletRequest request);
}
