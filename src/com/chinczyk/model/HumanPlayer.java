package com.chinczyk.model;

import com.chinczyk.logic.Game;

/**
 * Gracz sterowany przez człowieka. Implementuje takeTurn w sposób interaktywny.
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name, ColorType color) {
        super(name, color);
    }

    @Override
    public void takeTurn(Game game, int diceValue) {
        // UI obsługuje wybór pionka; tutaj nie wymuszamy automatycznego ruchu.
    }
}
