package route;

import java.awt.Point;

public class GreenRouteCalculator extends RouteCalculator {
    private final RedRouteCalculator red = new RedRouteCalculator();
    private int pawnCounter = 4; 

    @Override
    public Point getPosition(int counter) {
    
        if (counter < 0) {

            int[][] baza = { {10,1}, {10,2}, {11,1}, {11,2} }; 
            int idx = 4 - pawnCounter; 
            pawnCounter--;
            return new Point(baza[idx][1], baza[idx][0]);
        }
        Point p = red.getPosition(counter);
        int kolCzerw = p.x;
        int wCzerw = p.y;

        int kolZiel = 15 - 1 - wCzerw; 
        int wZiel = kolCzerw;


        return new Point(kolZiel, wZiel);
    }
}
