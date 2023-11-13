package com.vlzher.pollservice2.entity;

import javax.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionID;
    private String questionName;
    @ManyToOne
    @JoinColumn(name = "poll_id")
    private Poll poll;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> questionOptions;
}
