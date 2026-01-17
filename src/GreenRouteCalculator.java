import java.awt.Point;

public class GreenRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();

    @Override
    public Point getPosition(int counter) {
        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolZiel = 15 - 1 - wCzerw; // 14 - wCzerw
        int wZiel = kolCzerw;

        if (kolZiel < 0) kolZiel = 0;
        if (kolZiel >= 15) kolZiel = 14;
        if (wZiel < 0) wZiel = 0;
        if (wZiel >= 15) wZiel = 14;

        return new Point(kolZiel, wZiel);
    }
}
