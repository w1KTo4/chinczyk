package route;
import java.awt.Point;

public class RedRouteCalculator extends RouteCalculator {
    private int pawnCounter=4;
    @Override
    public Point getPosition(int counter) {
        int w = 0, k = 8; 

        if (counter <= 6) {         
            w = counter;
            k = 8;
        } else if (counter <= 12) {   
            w = 6;
            k = 8 + (counter - 6); 
        } else if (counter <= 14) {   
            w = 6 + (counter - 12); 
            k = 14;
        } else if (counter <= 20) {   
            w = 8;
            k = 13 - (counter - 14) + 1; 
        } else if (counter <= 26) {    
            w = 9 + (counter - 20) - 1;   
            k = 8;
        } else if (counter <= 28) {   
            w = 14;
            k = 8 - (counter - 26);  
        } else if (counter <= 34) {    
            w = 14 - (counter - 28);  
            k = 6;
        } else if (counter <= 40) {    
            w = 8;
            k = 6 - (counter - 34);  
        } else if (counter <= 42) {    
            w = 8 - (counter - 40);   
            k = 0;
        } else if (counter <= 48) {    
            w = 6;
            k = (counter - 42);  
        } else if (counter <= 54) {   
            w = 6 - (counter - 48);   
            k = 6;
        } else {                       
            w = pawnCounter+1;
            k = 6 + (counter - 54);
            pawnCounter--;  
        }


        return new Point(k, w);
    }
}
