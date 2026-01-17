public class Piece {
    private PieceColor color;
    private int counter; // numer pola na torze

    // Konstruktor domyślny
    public Piece() {
        this.color = PieceColor.RED;
        this.counter = 0;
    }

    // Konstruktor (kolor)
    public Piece(PieceColor color) {
        this.color = color;
        this.counter = 0;
    }

    // Konstruktor przeciążony (kolor + counter)
    public Piece(PieceColor color, int counter) {
        this.color = color;
        this.counter = counter;
    }

    // Przeciążone metody ruchu
    public void moveBy(int steps) {
        moveBy(steps, true);
    }

    public void moveBy(int steps, boolean capToEnd) {
        this.counter += steps;
        if (capToEnd && this.counter >= 56) this.counter = 55;
    }

    // gettery / settery (enkapsulacja)
    public PieceColor getColor() {
        return color;
    }

    public void setColor(PieceColor color) {
        this.color = color;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public java.awt.Color getAWTColor() {
        return color.getAwtColor();
    }

    @Override
    public String toString() {
        return "Piece{" +
                "color=" + color +
                ", counter=" + counter +
                '}';
    }
}
