package route;

import java.awt.Point;

public class YellowRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();
    private int pawnCounter = 4; 

    @Override
    public Point getPosition(int counter) {

        if (counter < 0) {
            int[][] baza = { {10,10}, {10,11}, {11,10}, {11,11} };
            int idx = 4 - pawnCounter;
            pawnCounter--;
            return new Point(baza[idx][1], baza[idx][0]);
        }

        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolZolty = 15 - 1 - kolCzerw;
        int wZolty = 15 - 1 - wCzerw;     


        return new Point(kolZolty, wZolty);
    }
}
