package com.turbochinczyk.backend.Tokens;

import com.turbochinczyk.backend.Board;

public class Player extends Token{
    private final Color color;
    private final Board board;
    public final int id;

    public Player(Color color, Board board, int id){
        this.color = color;
        this.board = board;
        this.id = id;
    }
    //teleportacja gracza jak przy spawnie
    public void go(int i){
        board.changeSquare(board.getTokenPos(this), new EmptySpace());
        board.changeSquare(i, this);
    }
    //prosty ruch do przodu używany przy ruchu gracza
    public void move(int i){
        int pos = board.getTokenPos(this);
        board.changeSquare(pos, new EmptySpace());
        pos+=i;
        board.changeSquare(pos, this);
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
