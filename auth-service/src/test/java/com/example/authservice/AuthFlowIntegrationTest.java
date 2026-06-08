package com.example.authservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.authservice.auth.JwtIssueRequest;
import com.example.authservice.auth.JwtIssueResponse;
import com.nimbusds.jwt.SignedJWT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void issuesJwtForExistingSessionContext() throws Exception {
        JwtIssueRequest request = new JwtIssueRequest(
                "external-session-token-123",
                "store-pos-web",
                new JwtIssueRequest.StoreClaims(
                        "store-001",
                        "BLR-KRM",
                        "Koramangala Flagship",
                        "Bengaluru",
                        "South",
                        Map.of("format", "FLAGSHIP", "inventoryZone", "BLR-SOUTH")
                ),
                new JwtIssueRequest.SalesmanClaims(
                        "S1001",
                        "Aarav Sales",
                        "SALESMAN",
                        Map.of("employeeCode", "EMP-S-1001", "counter", "C3")
                ),
                Set.of("STORE_STAFF")
        );

        ResponseEntity<JwtIssueResponse> authResponse = restTemplate.postForEntity(
                "/auth/jwt",
                request,
                JwtIssueResponse.class
        );

        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authResponse.getBody()).isNotNull();
        assertThat(authResponse.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getBody().jwtToken()).isNotBlank();
        assertThat(authResponse.getBody().expiresAt()).isNotNull();

        SignedJWT jwt = SignedJWT.parse(authResponse.getBody().jwtToken());
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("S1001");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("sid")).isEqualTo("external-session-token-123");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("app_id")).isEqualTo("store-pos-web");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("actor_type")).isEqualTo("SALESMAN");
        assertThat(jwt.getJWTClaimsSet().getStringListClaim("actor_groups")).containsExactly("STORE_STAFF");

        ResponseEntity<Map> jwks = restTemplate.getForEntity("/.well-known/jwks.json", Map.class);
        assertThat(jwks.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jwks.getBody()).containsKey("keys");

        ResponseEntity<Map> otpResponse = restTemplate.postForEntity(
                "/auth/otp/verify",
                Map.of("salesmanId", "S1001", "otp", "123456"),
                Map.class
        );
        assertThat(otpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
