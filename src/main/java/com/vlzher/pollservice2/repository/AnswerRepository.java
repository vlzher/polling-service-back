package com.vlzher.pollservice2.repository;

import com.vlzher.pollservice2.entity.Answer;
import com.vlzher.pollservice2.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByUserLogin(String userLogin);
    int countByQuestionOption(QuestionOption questionOption);
}