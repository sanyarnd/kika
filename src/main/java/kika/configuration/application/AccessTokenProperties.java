package kika.configuration.application;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import lombok.Getter;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
public final class AccessTokenProperties {
    private final Duration expirationTime;
    private final Key privateKey;
    private final Key publicKey;

    public AccessTokenProperties(String privateKey, String publicKey, Duration expirationTime) {
        try {
            byte[] publicKeyBytes = decodeBase64(publicKey);
            byte[] privateKeyBytes = decodeBase64(privateKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            this.expirationTime = expirationTime;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] decodeBase64(String encoded) {
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Public signing key must be a valid Base64 string", e);
        }
    }
}
