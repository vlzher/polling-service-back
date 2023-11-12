package com.vlzher.pollservice2.dto;
import com.vlzher.pollservice2.entity.Answer;
import lombok.Data;

import java.util.List;

@Data
public class PollDetailsDTO {
    private Long pollID;
    private String pollName;
    private List<QuestionDetailsDTO> questions;

}
