package com.example.authservice.auth;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

public record SalesmanAssignment(
        StoreContext store,
        SalesmanContext salesman,
        String otp
) {
}
