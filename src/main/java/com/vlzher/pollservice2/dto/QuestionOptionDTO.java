package com.vlzher.pollservice2.dto;
import com.vlzher.pollservice2.entity.QuestionOption;
import lombok.Data;

@Data
public class QuestionOptionDTO {
    private Long questionOptionID;
    private String questionOptionName;

    private int answerCount;
    public QuestionOptionDTO(QuestionOption questionOption, int answerCount){
        this.questionOptionID = questionOption.getQuestionOptionID();
        this.questionOptionName = questionOption.getQuestionOptionName();
        this.answerCount = answerCount;
    }
}
