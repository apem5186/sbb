package com.example.sbb.entity.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    FREE("FREEBOARD"),
    QUESTION("QUESTION");

    private final String value;

}
