package com.example.exampleservice;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authz.Authenticate;
import com.example.authz.RequestAuthContextHolder;

@RestController
public class StoreOrderController {
    @GetMapping("/orders")
    @Authenticate("STORE_ORDERS_READ")
    public Map<String, Object> orders() {
        var principal = RequestAuthContextHolder.requireCurrent();
        return Map.of(
                "storeId", principal.store().storeId(),
                "salesmanId", principal.salesman().salesmanId(),
                "actorType", principal.actorType()
        );
    }

    @GetMapping("/health/public")
    public Map<String, String> publicHealth() {
        return Map.of("status", "UP");
    }
}
