package com.vlzher.pollservice2.dto;
import lombok.Data;

@Data
public class RegistrationRequest {
    private String login;
    private String password;
}
