package com.example.sbb.exception;

public class OAuthProviderMissMatchException extends RuntimeException{

    public OAuthProviderMissMatchException(String message) {
        super(message);
    }
}
