package com.example.authservice.keys;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {
    private final JwtKeyPairProvider keyPairProvider;

    public JwksController(JwtKeyPairProvider keyPairProvider) {
        this.keyPairProvider = keyPairProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return keyPairProvider.jwkSet().toJSONObject();
    }
}
