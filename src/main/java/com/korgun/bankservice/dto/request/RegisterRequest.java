package com.korgun.bankservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private Set<String> roles;
}
