package com.turbochinczyk.backend;

public class Player {
    private final int userId;
    private final String username;
    private boolean ready;

    public Player(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.ready = false;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
}
