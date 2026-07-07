package com.turbochinczyk.backend.database;

public class UserSessionDto {
    private final int userId;
    private final String username;

    public UserSessionDto(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
}
