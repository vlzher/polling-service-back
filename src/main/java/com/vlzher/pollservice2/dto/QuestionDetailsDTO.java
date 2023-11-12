package com.vlzher.pollservice2.dto;
import com.vlzher.pollservice2.entity.Poll;
import com.vlzher.pollservice2.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDetailsDTO {
    private Long questionID;
    private String questionName;
    private List<QuestionOptionDTO> questionOptions;

    public QuestionDetailsDTO(Question question, List<QuestionOptionDTO> questionOptions) {
        this.questionID = question.getQuestionID();
        this.questionName = question.getQuestionName();
        this.questionOptions = questionOptions;
    }
}
