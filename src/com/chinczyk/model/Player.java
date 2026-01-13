package com.chinczyk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa bazowa gracza. Zawiera enkapsulację pól, gettery/settery, przeciążone konstruktory.
 */
public abstract class Player {
    private String name;
    private ColorType color;
    private List<Piece> pieces;
    private boolean active;

    public Player(String name, ColorType color) {
        this.name = name;
        this.color = color;
        this.pieces = new ArrayList<>();
        this.active = false;
        initPieces();
    }

    public Player(String name, ColorType color, int piecesCount) {
        this.name = name;
        this.color = color;
        this.pieces = new ArrayList<>(piecesCount);
        this.active = false;
        initPieces();
    }

    private void initPieces() {
        for (int i = 0; i < 4; i++) {
            pieces.add(new Piece(i, color));
        }
    }

    public String getName() {
        return name;
    }

    public ColorType getColor() {
        return color;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // metoda abstrakcyjna decydująca o ruchu - implementowana przez HumanPlayer i AIPlayer
    public abstract void takeTurn(com.chinczyk.logic.Game game, int diceValue);

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + ", color=" + color + '}';
    }
}
