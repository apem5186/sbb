package com.example.sbb.entity.user;

import com.example.sbb.entity.BaseEntity;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SiteUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(unique = true)
    private String sub;

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Answer> answerList = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Question> questionList = new ArrayList<>();


    public void modify(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /* 소셜로그인시 이미 등록된 회원이라면 수정날짜만 업데이트하고
     * 기존 데이터는 그대로 보존하도록 예외처리 */
    public SiteUser updateModifiedDate() {
        this.onPreUpdate();
        return this;
    }

    public String getRoleValue() {
        return this.role.getValue();
    }
}
