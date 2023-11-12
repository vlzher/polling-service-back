package com.vlzher.pollservice2.repository;

import com.vlzher.pollservice2.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByPollPollID(Long pollID);
}
