package com.example.sbb.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    SOCIAL("ROLE_SOCIAL");  // OAUTH

    private final String value;
}
