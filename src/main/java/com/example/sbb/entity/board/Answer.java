package com.example.sbb.entity.board;

import com.example.sbb.entity.BaseEntity;
import com.example.sbb.entity.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private SiteUser author;

    @ManyToOne
    private Question question;

    @ManyToMany
    Set<SiteUser> voter;


}
