package com.vlzher.pollservice2.entity;

import javax.persistence.*;
import lombok.Data;

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

}
