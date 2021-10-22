package kika.configuration.application;

import java.time.Duration;

/**
 * Cookie configuration
 *
 * @param path       Specifies a path for the cookie to which the client should return the cookie.
 *                   The cookie is visible to all the pages in the directory you specify, and all the pages
 *                   in that directory's subdirectories.
 * @param domain     Specifies the domain within which this cookie should be presented.
 *                   The form of the domain name is specified by RFC 2109. A domain name
 *                   begins with a dot (`.foo.com`) and means that the cookie is visible to servers
 *                   in a specified Domain Name System (DNS) zone (for example, `www.foo.com`, but not `a.b.foo.com`).
 * @param secure     Indicates to the browser whether the cookie should only be sent using a secure protocol,
 *                   such as HTTPS or SSL.
 * @param isHttpOnly sets the flag that controls if this cookie will be hidden from scripts on the client side.
 * @param maxAge     Sets the maximum age of the cookie.
 */
public record CookieProperties(
    String path,
    String domain,
    Boolean secure,
    Boolean isHttpOnly,
    Duration maxAge
) {
}
