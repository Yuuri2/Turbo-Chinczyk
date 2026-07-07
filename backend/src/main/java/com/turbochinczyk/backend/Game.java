package com.turbochinczyk.backend;

public class Game {
    private Board board;
    
    private int turn;
    // 0 - Red, 1 - Blue, 2 - Yellow, 3 - Green
    public Game(){
        chooseStartPlayer();
        while (true) {
            rollDice();
            nextTurn();
        }
    }


    private void nextTurn(){
        turn = (turn+1)%4;
    }

    private int rollDice(){
        return (int) (Math.random() * 6)+1;
    }

    private void chooseStartPlayer(){
        this.turn = (int) (Math.random() * 4);
    }
}
