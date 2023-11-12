package com.vlzher.pollservice2.dto;

import com.vlzher.pollservice2.entity.Poll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class PollDTO {
    private Long pollID;
    private String pollName;
    public PollDTO(Poll poll) {
        this.pollID = poll.getPollID();
        this.pollName = poll.getPollName();
    }
}
