package com.example.authservice.keys;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class PemUtils {
    private PemUtils() {
    }

    static RSAPrivateKey parsePrivateKey(String pem) {
        try {
            String normalized = normalize(pem, "PRIVATE KEY");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse RSA private key PEM", ex);
        }
    }

    static RSAPublicKey parsePublicKey(String pem) {
        try {
            String normalized = normalize(pem, "PUBLIC KEY");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(normalized)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse RSA public key PEM", ex);
        }
    }

    private static String normalize(String pem, String type) {
        return pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
    }
}
