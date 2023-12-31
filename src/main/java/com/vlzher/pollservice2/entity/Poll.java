package com.vlzher.pollservice2.entity;


import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "poll")
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pollID;
    private String pollName;
    @ManyToOne
    @JoinColumn(name = "user_login")
    private User user;
    @OneToMany(mappedBy = "poll", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Question> questions;
}