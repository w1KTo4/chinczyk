package com.chinczyk.model;

import com.chinczyk.logic.Game;

import java.util.Comparator;

/**
 * Prosty, ale ulepszony AI:
 * - preferuje zbicia (jeśli ruch spowoduje zbicie przeciwnika),
 * - potem wyjście z domu (jeśli możliwe),
 * - potem ruch pionkiem, który po ruchu będzie najdalej (maksymalizuje position).
 */
public class AIPlayer extends Player {

    public AIPlayer(String name, ColorType color) {
        super(name, color);
    }

    @Override
    public void takeTurn(Game game, int diceValue) {
        // 1) jeśli można zbijać, wybierz pionek, który zbije
        Piece bestCapture = null;
        for (Piece p : getPieces()) {
            if (p.getState() == PieceState.ON_BOARD) {
                int target = (p.getPosition() + diceValue) % Game.BOARD_SIZE;
                Tile t = game.getTileByIndex(target);
                if (t != null && !t.isEmpty() && t.getOccupant().getColor() != this.getColor()) {
                    bestCapture = p;
                    break;
                }
            }
        }
        if (bestCapture != null) {
            game.movePiece(this, bestCapture, diceValue);
            return;
        }

        // 2) jeśli można wyjść z domu, zrób to
        for (Piece p : getPieces()) {
            if (p.getState() == PieceState.HOME && diceValue == 6) {
                game.movePiece(this, p, diceValue);
                return;
            }
        }

        // 3) wybierz pionek, który po ruchu będzie najdalej (maksymalizuj index)
        Piece best = null;
        int bestPos = -1;
        for (Piece p : getPieces()) {
            if (p.getState() == PieceState.ON_BOARD) {
                int newPos = (p.getPosition() + diceValue) % Game.BOARD_SIZE;
                if (newPos > bestPos) {
                    bestPos = newPos;
                    best = p;
                }
            }
        }
        if (best != null) {
            game.movePiece(this, best, diceValue);
        } else {
            // nic do zrobienia
        }
    }
}
