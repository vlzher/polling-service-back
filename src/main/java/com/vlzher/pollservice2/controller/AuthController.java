package com.vlzher.pollservice2.controller;

import com.vlzher.pollservice2.dto.LoginRequest;
import com.vlzher.pollservice2.dto.RegistrationRequest;
import com.vlzher.pollservice2.entity.User;
import com.vlzher.pollservice2.service.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        try {
            // Create a new user
            User newUser = new User();
            newUser.setLogin(registrationRequest.getLogin());
            newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

            // Save the user to the database
            authService.registerUser(newUser);

            // Generate JWT token
            String token = authService.generateToken(newUser.getLogin());

            // Return the token in the response header
            return ResponseEntity.ok().header("Authorization", "Bearer " + token).build();
        } catch (DuplicateKeyException e) {
            // Handle duplicate login (username) error
            return new ResponseEntity("Username already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Handle other exceptions
            log.info(e.toString());
            return new ResponseEntity("Registration failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Void> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            String token = authService.authenticateUser(loginRequest.getLogin(), loginRequest.getPassword());
            return new ResponseEntity("Bearer " + token, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            // Handle invalid credentials error
            return new ResponseEntity("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // Handle other exceptions
            return new ResponseEntity("Login failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

