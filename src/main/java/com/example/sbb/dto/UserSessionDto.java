package com.example.sbb.dto;

import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.entity.user.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class UserSessionDto {
    private final Long id;
    private final String username;
    private final String email;
    private final UserRole userRole;
    private final String modDate;

    public UserSessionDto(SiteUser siteUser) {
        this.id = siteUser.getId();
        this.username = siteUser.getUsername();
        this.email = siteUser.getEmail();
        this.userRole = siteUser.getRole();
        this.modDate = siteUser.getModDate();
    }

}
