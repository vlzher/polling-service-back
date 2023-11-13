package com.vlzher.pollservice2.repository;

import com.vlzher.pollservice2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByLogin(String login);
    boolean existsByLogin(String login);
}