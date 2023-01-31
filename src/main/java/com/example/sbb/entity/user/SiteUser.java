package com.example.sbb.entity.user;

import com.example.sbb.entity.BaseEntity;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class SiteUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Answer> answerList = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Question> questionList = new ArrayList<>();

    public void modify(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
