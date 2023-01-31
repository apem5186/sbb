package com.example.sbb.entity.board;

import com.example.sbb.entity.BaseEntity;
import com.example.sbb.entity.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT") // columnDefinition = "TEXT"는 글자수를 제한할 수 없는 경우에 사용
    private String content;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int hits;

    @ManyToOne
    private SiteUser author;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answerList;

    // Set은 중복을 허용하지 않는 자료형이다.
    @ManyToMany
    Set<SiteUser> voter;
}
