package com.vlzher.pollservice2.dto;
import com.vlzher.pollservice2.entity.Answer;
import lombok.Data;

@Data
public class AnswerDTO {
    private Long answerID;
    private Long questionOptionID;

    public AnswerDTO(Answer answer) {
        this.answerID= answer.getAnswerID();
        this.questionOptionID = answer.getQuestionOption().getQuestionOptionID();
    }
}
