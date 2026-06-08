package com.example.authservice.auth;

import java.util.Set;

enum ActorType {
    STORE_ADMIN,
    SALESMAN,
    OPTOMETRIST,
    USHER,
    REMOTE_OPTOM,
    DISPENSING_OPTOM,
    KIDS_OPTOM,
    REPAIR_SPECIALIST;

    boolean isOptometrist() {
        return this == OPTOMETRIST
                || this == REMOTE_OPTOM
                || this == DISPENSING_OPTOM
                || this == KIDS_OPTOM;
    }

    Set<String> groups() {
        return isOptometrist() ? Set.of("OPTOMETRIST") : Set.of();
    }
}
