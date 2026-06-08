package com.example.authservice.auth;

public class UnknownActorTypeException extends RuntimeException {
    public UnknownActorTypeException(String actorType) {
        super("Unknown or inactive actor type: " + actorType);
    }
}
