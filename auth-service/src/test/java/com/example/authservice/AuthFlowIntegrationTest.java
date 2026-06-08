package com.example.authservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.authservice.auth.AuthResponse;
import com.example.authservice.auth.VerifyOtpRequest;
import com.example.authz.ActorType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void authenticatesWithJwtAndLogsOutSession() {
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                "/auth/otp/verify",
                new VerifyOtpRequest("S1001", "123456", "store-pos-web", "device-1"),
                AuthResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authResponse.getBody()).isNotNull();
        assertThat(authResponse.getBody().store().storeId()).isEqualTo("store-001");
        assertThat(authResponse.getBody().salesman().actorType()).isEqualTo(ActorType.SALESMAN);
        assertThat(authResponse.getBody().sessionToken()).isNotBlank();
        assertThat(authResponse.getBody().jwtToken()).isNotBlank();

        ResponseEntity<Map> jwks = restTemplate.getForEntity("/.well-known/jwks.json", Map.class);
        assertThat(jwks.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jwks.getBody()).containsKey("keys");

        ResponseEntity<Void> logoutResponse = restTemplate.exchange(
                "/auth/sessions/{sessionToken}",
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class,
                authResponse.getBody().sessionToken()
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
