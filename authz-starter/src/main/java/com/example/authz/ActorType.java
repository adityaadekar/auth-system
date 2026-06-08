package com.example.authz;

import java.util.Set;

public enum ActorType {
    STORE_ADMIN,
    SALESMAN,
    OPTOMETRIST,
    USHER,
    REMOTE_OPTOM,
    DISPENSING_OPTOM,
    KIDS_OPTOM,
    REPAIR_SPECIALIST;

    public boolean isOptometrist() {
        return this == OPTOMETRIST
                || this == REMOTE_OPTOM
                || this == DISPENSING_OPTOM
                || this == KIDS_OPTOM;
    }

    public Set<String> groups() {
        return isOptometrist() ? Set.of("OPTOMETRIST") : Set.of();
    }
}
