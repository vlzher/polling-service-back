package com.vlzher.pollservice2.service;

import com.vlzher.pollservice2.dto.RegistrationRequest;
import com.vlzher.pollservice2.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    User registerUser(RegistrationRequest userRegistrationDTO);

    User getUserByUsername(String username);

    UserDetails loadUserByUsername(String username);
}
