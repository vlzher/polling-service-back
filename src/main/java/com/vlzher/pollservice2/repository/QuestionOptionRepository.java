package com.vlzher.pollservice2.repository;

import com.vlzher.pollservice2.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionQuestionID(Long questionID);
}
