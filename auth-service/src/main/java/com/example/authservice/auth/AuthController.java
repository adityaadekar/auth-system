package com.example.authservice.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.session.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final OtpAuthenticationService otpAuthenticationService;
    private final JwtIssuer jwtIssuer;
    private final SessionService sessionService;

    public AuthController(
            OtpAuthenticationService otpAuthenticationService,
            JwtIssuer jwtIssuer,
            SessionService sessionService
    ) {
        this.otpAuthenticationService = otpAuthenticationService;
        this.jwtIssuer = jwtIssuer;
        this.sessionService = sessionService;
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return otpAuthenticationService.authenticate(request);
    }

    @PostMapping("/jwt/exchange")
    public JwtExchangeResponse exchange(@Valid @RequestBody ExchangeSessionRequest request) {
        return jwtIssuer.exchange(request);
    }

    @DeleteMapping("/sessions/{sessionToken}")
    public ResponseEntity<Void> logout(@PathVariable("sessionToken") String sessionToken) {
        sessionService.logout(sessionToken);
        return ResponseEntity.noContent().build();
    }
}
