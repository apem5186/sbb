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
    private final Integer id;
    private final String username;
    private final String email;
    private final UserRole userRole;

    private final String sub;
    private final LocalDateTime modDate;

    public UserSessionDto(SiteUser siteUser) {
        this.id = siteUser.getId();
        this.username = siteUser.getUsername();
        this.email = siteUser.getEmail();
        this.userRole = siteUser.getRole();
        this.sub = siteUser.getSub();
        this.modDate = siteUser.getModDate();
    }

}
