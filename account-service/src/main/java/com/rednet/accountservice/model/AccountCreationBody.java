package com.rednet.accountservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountCreationBody (
    @NotBlank(message = "Username min length is 1")
    String username,

    @Email(message = "Invalid email")
    @NotBlank(message = "Invalid email")
    String email,

    @NotBlank(message = "Password min length is 1")
    String password,

    @NotBlank(message = "Secret word min length is 1")
    String secretWord,

    @Size(min = 1, message = "There is should be at least one role")
    String[] roles
) {}
