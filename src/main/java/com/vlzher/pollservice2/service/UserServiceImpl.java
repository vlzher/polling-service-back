package com.vlzher.pollservice2.service;

import com.vlzher.pollservice2.dto.RegistrationRequest;
import com.vlzher.pollservice2.entity.User;
import com.vlzher.pollservice2.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegistrationRequest userRegistrationDTO) {
        // Validate input, check if username is unique, etc.

        // Create a new user entity
        User user = new User();
        user.setLogin(userRegistrationDTO.getLogin());
        user.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
        // Set other user properties...

        // Save the user in the database
        return userRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByLogin(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.emptyList());
    }
}
