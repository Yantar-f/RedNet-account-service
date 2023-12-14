package com.rednet.accountservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(
        name = "accounts",

        uniqueConstraints = {
                @UniqueConstraint(name = "unique_username_constraint", columnNames = "username"),
                @UniqueConstraint(name = "unique_email_constraint", columnNames = "email")
        },

        indexes = {
                @Index(name = "username_index", columnList = "username"),
                @Index(name = "email_index", columnList = "email")
        })
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Digits(fraction = 0, integer = 0)
    @NotBlank(message = "ID min length is 1")
    @Column(name = "account_id")
    private long ID;

    @Column(name = "username")
    @NotBlank(message = "Username min length is 1")
    private String username;

    @Column(name = "email")
    @Email(message = "Invalid email")
    private String email;

    @Column(name = "password")
    @NotBlank(message = "Password min length is 1")
    private String password;

    @Column(name = "secret_word")
    @NotBlank(message = "Secret word min length is 1")
    private String secretWord;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "accounts_to_roles",
            joinColumns = @JoinColumn (name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Size(min = 1, message = "There is should be at least one role")
    private List<Role> roles;

    protected Account() {}
    public Account(
            String username,
            String email,
            String password,
            String secretWord,
            List<Role> roles
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.secretWord = secretWord;
        this.roles = roles;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
