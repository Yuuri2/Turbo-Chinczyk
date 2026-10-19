package com.turbochinczyk.backend;
import com.turbochinczyk.backend.Tokens.EmptySpace;
import com.turbochinczyk.backend.Tokens.Player;
import com.turbochinczyk.backend.Tokens.Player.Color;
import com.turbochinczyk.backend.Tokens.Token;

public class Board {
    //Wszystkie ważne miejsca na planszy są przechowywane w ciągu. Ułatwi to prouszanie po planszy bo player.move(5) przesunie
    // pionek w tablicy o 5
    private Token[] boardSpaces;
    private Token[] offBoardSpaces;
    private enum specialTiles{
        BlueSpawn(0),
        YellowSpawn(13),
        GreenSpawn(26),
        RedSpawn(39),

        BackToBlue(5),
        BackToYellow(18),
        BackToGreen(31),
        BackToRed(44),

        JumpBTY(3),
        JumpYTY(8),
        JumpYTG(16),
        JumpGTG(21),
        JumpGTR(29),
        JumpRTR(34),
        JumpRTB(42),
        JumpBTB(47);


        private final int index;
        specialTiles(int index){
            this.index = index;
        }
        public int getIndex(){
            return this.index;
        }
        public static specialTiles getByIndex(int index){
            for(specialTiles tile : specialTiles.values()){
                if(tile.getIndex() == index){
                    return tile;
                }
            }
            return null;
        }
    }

    private Token[] yellowBase;
    private Token[] yellowToSpawn;
    private Token[] redBase;
    private Token[] redToSpawn;
    private Token[] blueBase;
    private Token[] blueToSpawn;
    private Token[] greenBase;
    private Token[] greenToSpawn;
    //
    //BOARD OPERATIONS
    //
    public Board(){
        initalizeBoard();
    }

    public Token getBoardToken(int i){
        return this.boardSpaces[i];
    }
    //Sprawdza czy na danym polu jest specjalna akcja i wykonuje ją jeśli tak. W przeciwnym wypadku ustawia nowy token na planszy w miejscu indeksu i.
    public void changeBoardToken(int i, Token newToken){
        if(isDestinationSpecial(i, (Player) newToken)){
            handleSpecialTile(specialTiles.getByIndex(i), (Player) newToken);
        }
        else{
            this.boardSpaces[i] = newToken;
        }
    }
    //Zarządza specjalnymi polami na planszy.
    public boolean isDestinationSpecial(int i, Player player){
        specialTiles tile = specialTiles.getByIndex(i);
        return tile != null;
    }
    // Obsługuje specjalne pola na planszy.
    private void handleSpecialTile(specialTiles tile, Player player){
        switch(tile){
            case BackToBlue:
                player.go(specialTiles.BlueSpawn.getIndex());
                break;
            case BackToYellow:
                player.go(specialTiles.YellowSpawn.getIndex());
                break;
            case BackToGreen:
                player.go(specialTiles.GreenSpawn.getIndex());
                break;
            case BackToRed:
                player.go(specialTiles.RedSpawn.getIndex());
                break;
            case JumpBTY:
                player.go(specialTiles.JumpYTY.getIndex());
                break;
            case JumpYTY:
                player.go(specialTiles.JumpYTG.getIndex());
                break;
            case JumpYTG:
                player.go(specialTiles.JumpGTG.getIndex());
                break;
            case JumpGTG:
                player.go(specialTiles.JumpGTR.getIndex());
                break;
            case JumpGTR:
                player.go(specialTiles.JumpRTR.getIndex());
                break;
            case JumpRTR:
                player.go(specialTiles.JumpRTB.getIndex());
                break;
            case JumpRTB:
                player.go(specialTiles.JumpBTB.getIndex());
                break;
            case JumpBTB:
                player.go(specialTiles.JumpBTY.getIndex());
                break;
            // Baza jest specjalnym polem, ale nie ma żadnej akcji z nim związanej. Ten default ma to łapać.
            default:
                player.go(tile.getIndex());
                break;
        }
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
        
        loadPlayer(Color.Blue, blueBase, blueToSpawn);
        loadPlayer(Color.Yellow, yellowBase, yellowToSpawn);
        loadPlayer(Color.Green, greenBase, greenToSpawn);
        loadPlayer(Color.Red, redBase, redToSpawn);

    }

    private void loadPlayer(Color color, Token[] baseList, Token[] respawnList){
        for(int i=0; i < 5; i++){
            respawnList[i] = new Player(color, this, i);
            baseList[i] = new EmptySpace();
        }
    }

    private void showBoard(){
        //Print planszy
    }
}
