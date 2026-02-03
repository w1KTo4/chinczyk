package model;
import java.awt.Color;

public enum PieceColor {
    RED(new Color(153,0,0)),
    GREEN(new Color(0,153,0)),
    BLUE(new Color(0,0,153)),
    YELLOW(new Color(204,204,0));

    private final Color Color;

    PieceColor(Color awtColor) {
        this.Color = awtColor;
    }

    public Color getAwtColor() {
        return Color;
    }
}
