package com.turbochinczyk.backend;

public class Game {
    private Board board = new Board();

    public enum Turn {
        Red,
        Blue,
        Yellow,
        Urple,
        Green;

        private static final Turn[] TURNS = values();

        public Turn next() {
            return TURNS[(this.ordinal() + 1) % TURNS.length];
        }

        public static Turn random() {
            return TURNS[(int)(Math.random() * TURNS.length)];
        }
    }
    
    private Turn turn;
    // proszę nie obraź się na mnie ale zmieniłem to na enum

    public Game(){
        chooseStartPlayer();
    }


    public void nextTurn(){
        this.turn = this.turn.next();
    }

    public static int rollDice(){
        return (int) (Math.random() * 6)+1;
    }

    private void chooseStartPlayer(){
        this.turn = Turn.random();
    }
}
