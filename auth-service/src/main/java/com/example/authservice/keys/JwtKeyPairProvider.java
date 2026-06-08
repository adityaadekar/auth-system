package com.example.authservice.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.authservice.auth.AuthProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

@Component
public class JwtKeyPairProvider {
    private final AuthProperties properties;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyPairProvider(AuthProperties properties) {
        this.properties = properties;
        if (StringUtils.hasText(properties.getJwt().getPrivateKeyPem())) {
            this.privateKey = PemUtils.parsePrivateKey(properties.getJwt().getPrivateKeyPem());
            this.publicKey = PemUtils.parsePublicKey(properties.getJwt().getPublicKeyPem());
        } else {
            KeyPair keyPair = generateEphemeralKeyPair();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
        }
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public String keyId() {
        return properties.getJwt().getKeyId();
    }

    public JWKSet jwkSet() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(keyId())
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
        return new JWKSet(rsaKey);
    }

    private KeyPair generateEphemeralKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate local RSA key pair", ex);
        }
    }
}
