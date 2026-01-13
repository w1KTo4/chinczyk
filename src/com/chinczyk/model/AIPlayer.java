package com.chinczyk.model;

import com.chinczyk.logic.Game;

/**
 * Prosty gracz AI. Pokazuje polimorfizm: inna implementacja takeTurn.
 */
public class AIPlayer extends Player {

    public AIPlayer(String name, ColorType color) {
        super(name, color);
    }

    @Override
    public void takeTurn(Game game, int diceValue) {
        // prosta heurystyka: porusz pierwszy możliwy pionek
        for (Piece p : getPieces()) {
            if (game.canMovePiece(this, p, diceValue)) {
                game.movePiece(this, p, diceValue);
                break;
            }
        }
    }
}
