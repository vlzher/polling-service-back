package com.vlzher.pollservice2.service;

import com.vlzher.pollservice2.entity.User;
import com.vlzher.pollservice2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.ArrayList;
import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User newUser) {
        // Check if the user already exists
        if (userRepository.existsByLogin(newUser.getLogin())) {
            throw new DuplicateKeyException("Username already exists");
        }

        // Save the user to the database
        userRepository.save(newUser);
    }

    public String generateToken(String username) {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // You may need to implement loadUserByUsername from UserDetailsService
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }


        // Convert User to UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    public String authenticateUser(String login, String password) {
        // Authenticate the user based on login and password
        User user = userRepository.findByLogin(login);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // User authenticated successfully, generate and return the token
            return generateToken(login);
        } else {
            // Authentication failed
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
