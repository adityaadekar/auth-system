package com.example.authservice.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

@Repository
public class SalesmanDirectory {
    private final Map<String, SalesmanAssignment> assignments = new LinkedHashMap<>();

    public SalesmanDirectory() {
        StoreContext koramangala = new StoreContext(
                "store-001",
                "BLR-KRM",
                "Koramangala Flagship",
                "Bengaluru",
                "South",
                Map.of("format", "FLAGSHIP", "inventoryZone", "BLR-SOUTH", "timezone", "Asia/Kolkata")
        );
        StoreContext indiranagar = new StoreContext(
                "store-002",
                "BLR-IND",
                "Indiranagar Clinic",
                "Bengaluru",
                "South",
                Map.of("format", "CLINIC", "inventoryZone", "BLR-EAST", "timezone", "Asia/Kolkata")
        );

        add(koramangala, "S1001", "Aarav Sales", ActorType.SALESMAN, "123456",
                Map.of("employeeCode", "EMP-S-1001", "counter", "C3"));
        add(koramangala, "A1001", "Diya Admin", ActorType.STORE_ADMIN, "123456",
                Map.of("employeeCode", "EMP-A-1001", "adminLevel", "STORE"));
        add(indiranagar, "O1001", "Riya Remote Optom", ActorType.REMOTE_OPTOM, "123456",
                Map.of("employeeCode", "EMP-O-1001", "license", "OPT-9981"));
        add(indiranagar, "D1001", "Kabir Dispensing Optom", ActorType.DISPENSING_OPTOM, "123456",
                Map.of("employeeCode", "EMP-D-1001", "license", "OPT-1190"));
    }

    public Optional<SalesmanAssignment> find(String salesmanId) {
        return Optional.ofNullable(assignments.get(salesmanId));
    }

    private void add(
            StoreContext store,
            String salesmanId,
            String displayName,
            ActorType actorType,
            String otp,
            Map<String, Object> attributes
    ) {
        assignments.put(salesmanId, new SalesmanAssignment(
                store,
                new SalesmanContext(salesmanId, displayName, actorType.name(), attributes),
                otp
        ));
    }
}
