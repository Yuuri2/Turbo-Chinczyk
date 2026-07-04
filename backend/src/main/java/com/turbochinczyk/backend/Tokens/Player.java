package com.turbochinczyk.backend.Tokens;

public class Player extends Token{
    private final Color color;

    public Player(Color color){
        this.color = color;
    }

    public Color getColor(){
        return this.color;
    }

    public enum Color{
        Yellow,
        Blue,
        Red,
        Green
    }
}
