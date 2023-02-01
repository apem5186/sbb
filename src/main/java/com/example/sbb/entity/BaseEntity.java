package com.example.sbb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@MappedSuperclass // 공통 매핑 정보가 필요할 때 사용
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "regDate", updatable = false)
    private LocalDateTime regDate;

    @LastModifiedDate
    @Column(name = "modDate")
    private LocalDateTime modDate;

    @PrePersist
    public void onPrePersist() {
        this.regDate = LocalDateTime.now();
        this.modDate = this.regDate;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.modDate = LocalDateTime.now();
    }
}
