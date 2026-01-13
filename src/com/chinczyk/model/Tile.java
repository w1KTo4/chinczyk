package com.chinczyk.model;

import java.awt.Graphics;

/**
 * Pojedyncze pole trasy. Zawiera współrzędne planszy (row/col 1..15),
 * indeks w trasie (0..51) oraz occupant jeśli pionek stoi na polu.
 */
public class Tile {
    private int index; // indeks na trasie 0..51
    private int row;   // 1..15
    private int col;   // 1..15
    private ColorType color;
    private Piece occupant;

    public Tile(int index, int row, int col, ColorType color) {
        this.index = index;
        this.row = row;
        this.col = col;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public ColorType getColor() {
        return color;
    }

    public Piece getOccupant() {
        return occupant;
    }

    public void setOccupant(Piece occupant) {
        this.occupant = occupant;
    }

    public boolean isEmpty() {
        return occupant == null;
    }

    // opcjonalne rysowanie, nie używane bezpośrednio w BoardPanel
    public void draw(Graphics g, int x, int y, int size) {
        // implementacja zależna od UI
    }
}
