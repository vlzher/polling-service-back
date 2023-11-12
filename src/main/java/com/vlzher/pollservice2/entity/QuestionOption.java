package com.vlzher.pollservice2.entity;

import javax.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "questionOption")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionOptionID;
    private String questionOptionName;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}