package com.example.sbb.dto;

import com.example.sbb.entity.user.SiteUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfileDTO {

    private Integer id;

    private String username;

    private String email;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    private String providerId;
    private String provider;

    public ProfileDTO(SiteUser siteUser) {
        this.id = siteUser.getId();
        this.username = siteUser.getUsername();
        this.email = siteUser.getEmail();
        this.regDate = siteUser.getRegDate();
        this.modDate = siteUser.getModDate();
        this.providerId = siteUser.getProviderId();
        this.provider = siteUser.getProvider();
    }
}
