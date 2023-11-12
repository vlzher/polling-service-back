package com.vlzher.pollservice2.dto;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private String questionName;
    private List<String> questionOptions;
}
