package com.example.authz;

import java.util.Map;

public record StoreContext(
        String storeId,
        String storeCode,
        String name,
        String city,
        String region,
        Map<String, Object> attributes
) {
}
