package com.chinczyk.model;

import com.chinczyk.logic.Movable;

/**
 * Abstrakcyjna klasa pionka. Zawiera wspólną logikę i abstrakcyjną metodę specjalnego ruchu.
 */
public abstract class AbstractPiece implements Movable {
    protected int id;
    protected ColorType color;
    protected PieceState state;
    protected int position; // -1 = HOME, 0..51 = index na trasie, -2 = FINISHED

    public AbstractPiece(int id, ColorType color) {
        this.id = id;
        this.color = color;
        this.state = PieceState.HOME;
        this.position = -1;
    }

    public AbstractPiece(int id, ColorType color, PieceState state, int position) {
        this.id = id;
        this.color = color;
        this.state = state;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public ColorType getColor() {
        return color;
    }

    public PieceState getState() {
        return state;
    }

    public void setState(PieceState state) {
        this.state = state;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // abstrakcyjna metoda specjalnego ruchu, implementowana przez konkretne klasy
    public abstract void specialMove(int steps);

    @Override
    public String toString() {
        return "Piece{" + "id=" + id + ", color=" + color + ", state=" + state + ", pos=" + position + '}';
    }
}
