package com.example.sbb.dto;

import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class AnswerForm {
    @NotEmpty(message = "내용은 필수사항입니다.")
    private String content;

}
