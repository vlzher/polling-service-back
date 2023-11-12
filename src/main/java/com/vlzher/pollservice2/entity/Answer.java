package com.vlzher.pollservice2.entity;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "answer")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerID;

    @ManyToOne
    @JoinColumn(name = "question_option_id")
    private QuestionOption questionOption;

    @ManyToOne
    @JoinColumn(name = "user1_login")
    private User user;
}
