package route;
import java.awt.Point;

public class RedRouteCalculator extends RouteCalculator {

    @Override
    public Point getPosition(int counter) {
        int w = 0, k = 8; // start: górny środek

        if (counter <= 6) {            // w dół 6 pól
            w = counter;
            k = 8;
        } else if (counter <= 12) {    // w prawo 6 pól
            w = 6;
            k = 8 + (counter - 6); // pola: 8..13
        } else if (counter <= 14) {    // w dół 2 pola
            w = 6 + (counter - 12); // 7..8
            k = 14;
        } else if (counter <= 20) {    // w lewo 6 pól
            w = 8;
            k = 13 - (counter - 14) + 1; // 12..7
        } else if (counter <= 26) {    // w dół 6 pól
            w = 9 + (counter - 20) - 1;   // 9..14
            k = 8;
        } else if (counter <= 28) {    // w lewo 2 pola
            w = 14;
            k = 8 - (counter - 26);   // 6..5
        } else if (counter <= 34) {    // w górę 6 pól
            w = 14 - (counter - 28);  // 14..9
            k = 6;
        } else if (counter <= 40) {    // w lewo 6 pól
            w = 8;
            k = 6 - (counter - 34);   // 4..-1 → ograniczamy na końcu
        } else if (counter <= 42) {    // w górę 2 pola
            w = 8 - (counter - 40);   // 7..6
            k = 0;
        } else if (counter <= 48) {    // w prawo 6 pól
            w = 6;
            k = (counter - 42);   // 1..6
        } else if (counter <= 54) {    // w górę 6 pól
            w = 6 - (counter - 48);   // 5..0
            k = 6;
        } else {                       // w prawo 2 pola do pola startowego
            w = 0;
            k = 6 + (counter - 54);    // końcowy segment
        }

        // ograniczenie do planszy (nadrzędnie - Panel/Board też zabezpieczają)
        if (w < 0) w = 0;
        if (w >= 15) w = 14;
        if (k < 0) k = 0;
        if (k >= 15) k = 14;

        return new Point(k, w);
    }
}
