package com.turbochinczyk.backend;

public class Game {
    // private Board board = new Board();

    public enum Turn {
        Red,
        Blue,
        Yellow,
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

    public void nextTurn(){
        this.turn = this.turn.next();
    }

    public static int rollDice(){
        return (int) (Math.random() * 6)+1;
    }

    private void chooseStartPlayer(){
        this.turn = Turn.random();
    }

    public Game(){
        chooseStartPlayer();
    }

    public static void main(String[] args){
        Game game = new Game();
        game.chooseStartPlayer();
        System.out.println(game.turn);
        game.nextTurn();
        System.out.println(game.turn);
    }
}
