import java.awt.Point;

public class BlueRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();

    @Override
    public Point getPosition(int counter) {
        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolNieb = wCzerw;
        int wNieb = 15 - 1 - kolCzerw; // 14 - kolCzerw

        if (kolNieb < 0) kolNieb = 0;
        if (kolNieb >= 15) kolNieb = 14;
        if (wNieb < 0) wNieb = 0;
        if (wNieb >= 15) wNieb = 14;

        return new Point(kolNieb, wNieb);
    }
}
