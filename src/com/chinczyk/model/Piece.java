package com.chinczyk.model;

/**
 * Konkretna implementacja pionka. Przeciążone konstruktory, nadpisanie metod.
 */
public class Piece extends AbstractPiece {

    public Piece(int id, ColorType color) {
        super(id, color);
    }

    public Piece(int id, ColorType color, int startPosition) {
        super(id, color, PieceState.ON_BOARD, startPosition);
    }

    // implementacja interfejsu Movable
    @Override
    public void move(int steps) {
        if (state == PieceState.HOME && steps == 6) {
            // wyjście z domu na start - ustawiane przez Game (tutaj tylko przykładowo)
            this.position = 0;
            this.state = PieceState.ON_BOARD;
        } else if (state == PieceState.ON_BOARD) {
            this.position += steps;
        }
    }

    // przeciążona wersja move z dodatkową flagą (przykład przeciążenia metod)
    public void move(int steps, boolean force) {
        if (force) {
            this.position = Math.max(0, this.position + steps);
            this.state = PieceState.ON_BOARD;
        } else {
            move(steps);
        }
    }

    @Override
    public void specialMove(int steps) {
        // prosty przykład: jeśli wyrzucisz 6, możesz wykonać dodatkowy krok
        if (steps == 6 && state == PieceState.ON_BOARD) {
            this.position += 1;
        }
    }
}
