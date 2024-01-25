package com.rednet.accountservice.model;

public class AccountUniqueFieldsOccupancy {
    private String username;
    private String email;
    private boolean isUsernameOccupied;
    private boolean isEmailOccupied;

    public AccountUniqueFieldsOccupancy(String username,
                                        String email,
                                        boolean isUsernameOccupied,
                                        boolean isEmailOccupied) {
        this.username = username;
        this.email = email;
        this.isUsernameOccupied = isUsernameOccupied;
        this.isEmailOccupied = isEmailOccupied;
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

    public boolean isUsernameOccupied() {
        return isUsernameOccupied;
    }

    public void setUsernameOccupied(boolean usernameOccupied) {
        isUsernameOccupied = usernameOccupied;
    }

    public boolean isEmailOccupied() {
        return isEmailOccupied;
    }

    public void setEmailOccupied(boolean emailOccupied) {
        isEmailOccupied = emailOccupied;
    }

    public boolean isAnyOccupied() {
        return isUsernameOccupied || isEmailOccupied;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        AccountUniqueFieldsOccupancy fields = (AccountUniqueFieldsOccupancy) obj;

        return  username.equals(fields.username) &&
                email.equals(fields.email) &&
                isUsernameOccupied == fields.isUsernameOccupied &&
                isEmailOccupied == fields.isEmailOccupied;
    }

    @Override
    public int hashCode() {
        return  username.hashCode() *
                email.hashCode() *
                Boolean.hashCode(isUsernameOccupied) *
                Boolean.hashCode(isEmailOccupied);
    }
}
