package route;

import java.awt.Point;

public class BlueRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();
    private int pawnCounter = 4; 

    @Override
    public Point getPosition(int counter) {

        if (counter < 0) {
            int[][] baza = { {1,10}, {1,11}, {2,10}, {2,11} };
            int idx = 4 - pawnCounter;
            pawnCounter--;
            return new Point(baza[idx][1], baza[idx][0]);
        }


        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolNieb = wCzerw;
        int wNieb = 15 - 1 - kolCzerw; 

        return new Point(kolNieb, wNieb);
    }
}
