package com.vlzher.pollservice2.repository;

import com.vlzher.pollservice2.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByUserLogin(String Login);
}