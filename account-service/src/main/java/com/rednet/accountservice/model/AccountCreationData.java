package com.rednet.accountservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public record AccountCreationData(
    @NotBlank(message = "Username min length is 1")
    String username,

    @NotBlank(message = "Invalid email")
    String email,

    @NotBlank(message = "Password min length is 1")
    String password,

    @NotBlank(message = "Secret word min length is 1")
    String secretWord,

    @NotNull
    @Size(min = 1, message = "There is should be at least one role")
    String[] roles
) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        AccountCreationData data = (AccountCreationData) obj;

        return  username.equals(data.username) &&
                email.equals(data.email) &&
                password.equals(data.password) &&
                secretWord.equals(data.secretWord) &&
                roles.length == data.roles.length &&
                new HashSet<>(List.of(roles)).containsAll(new HashSet<>(List.of(data.roles)));
    }

    @Override
    public int hashCode() {
        return  username.hashCode() *
                email.hashCode() *
                password.hashCode() *
                secretWord.hashCode() *
                Arrays.hashCode(roles);
    }

    @Override
    public String toString() {
        return "{\n" +
                "\n\tusername: " + username +
                "\n\temail: " + email +
                "\n\tpassword: " + password +
                "\n\tsecretWord: " + secretWord +
                "\n\troles: " + Arrays.toString(roles) +
                "\n}";
    }
}
