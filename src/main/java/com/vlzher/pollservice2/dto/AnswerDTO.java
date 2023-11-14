package com.vlzher.pollservice2.dto;
import com.vlzher.pollservice2.entity.Answer;
import lombok.Data;

@Data
public class AnswerDTO {
    private String questionName;
    private String optionName;
    public AnswerDTO(Answer answer) {
        this.questionName = answer.getQuestionOption().getQuestion().getQuestionName();
        this.optionName = answer.getQuestionOption().getQuestionOptionName();
    }
}
