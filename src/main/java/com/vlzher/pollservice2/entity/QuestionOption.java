package com.vlzher.pollservice2.entity;

import javax.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "question_option")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_option_id")
    private Long questionOptionID;
    @Column(name = "question_option_name")
    private String questionOptionName;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
    @OneToMany(mappedBy = "questionOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers;
}