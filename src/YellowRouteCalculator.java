import java.awt.Point;

public class YellowRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();

    @Override
    public Point getPosition(int counter) {
        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolZolty = 15 - 1 - kolCzerw;
        int wZolty = 15 - 1 - wCzerw;

        if (kolZolty < 0) kolZolty = 0;
        if (kolZolty >= 15) kolZolty = 14;
        if (wZolty < 0) wZolty = 0;
        if (wZolty >= 15) wZolty = 14;

        return new Point(kolZolty, wZolty);
    }
}
