package com.example.authservice.auth;

import org.springframework.stereotype.Service;

import com.example.authservice.session.SessionRecord;
import com.example.authservice.session.SessionService;

@Service
public class OtpAuthenticationService {
    private final SalesmanDirectory salesmanDirectory;
    private final SessionService sessionService;
    private final JwtIssuer jwtIssuer;

    public OtpAuthenticationService(
            SalesmanDirectory salesmanDirectory,
            SessionService sessionService,
            JwtIssuer jwtIssuer
    ) {
        this.salesmanDirectory = salesmanDirectory;
        this.sessionService = sessionService;
        this.jwtIssuer = jwtIssuer;
    }

    public AuthResponse authenticate(VerifyOtpRequest request) {
        SalesmanAssignment assignment = salesmanDirectory.find(request.salesmanId())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid salesmanId or OTP"));
        if (!assignment.otp().equals(request.otp())) {
            throw new AuthenticationFailedException("Invalid salesmanId or OTP");
        }

        SessionRecord session = sessionService.create(
                request.applicationId(),
                request.deviceId(),
                assignment.store(),
                assignment.salesman()
        );
        return new AuthResponse(
                assignment.store(),
                assignment.salesman(),
                session.sessionToken(),
                jwtIssuer.issue(session),
                session.expiresAt()
        );
    }
}
