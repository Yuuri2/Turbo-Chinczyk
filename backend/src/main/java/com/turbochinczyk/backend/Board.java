package com.turbochinczyk.backend;
import com.turbochinczyk.backend.Tokens.EmptySpace;
import com.turbochinczyk.backend.Tokens.Player;
import com.turbochinczyk.backend.Tokens.Player.Color;
import com.turbochinczyk.backend.Tokens.Token;

public class Board {
    //Wszystkie ważne miejsca na planszy są przechowywane w ciągu. Ułatwi to prouszanie po planszy bo player.move(5) przesunie
    // pionek w tablicy o 5
    private Token[] boardSpaces;

    private Token[] yellowBase;
    private Token[] yellowToSpawn;
    private Token[] redBase;
    private Token[] redToSpawn;
    private Token[] blueBase;
    private Token[] blueToSpawn;
    private Token[] greenBase;
    private Token[] greenToSpawn;


    public Board(){
        initalizeBoard();
    }

    public Token getBoardSquare(int i){
        return this.boardSpaces[i];
    }

    public void changeSquare(int i, Token newToken){
        this.boardSpaces[i] = newToken;
    }

    public int getTokenPos(Token token){
        if (token instanceof Player player){
            for(int i=0; i < boardSpaces.length;i++){
                if(player.id == ((Player) boardSpaces[i]).id && player.getColor() == ((Player) boardSpaces[i]).getColor()){
                    return i;
                }
            }
        }
        else{
            for(int i=0; i < boardSpaces.length;i++){
                if(token == boardSpaces[i]){
                    return i;
                }
            }
        }
        
        return -1;
    }

    private void initalizeBoard(){
        for(int i=0; i<52;i++){
            this.boardSpaces[i] = new EmptySpace();
        }
        loadPlayers(Color.Blue, blueBase, blueToSpawn);
        loadPlayers(Color.Yellow, yellowBase, yellowToSpawn);
        loadPlayers(Color.Green, greenBase, greenToSpawn);
        loadPlayers(Color.Red, redBase, redToSpawn);

    }
    private void loadPlayers(Color color, Token[] baseList, Token[] respawnList){
        for(int i=0; i < 4; i++){
            respawnList[i] = new Player(color, this, i);

            baseList[i] = new EmptySpace();
        }
    }

    private void showBoard(){
        //Print planszy
    }
}
