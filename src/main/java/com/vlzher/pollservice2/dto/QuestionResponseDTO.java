package com.vlzher.pollservice2.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionResponseDTO {
    private Long questionID;
    private String questionName;
    private List<QuestionOptionDTO> questionOptions;
}

