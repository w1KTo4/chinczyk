package route;
import java.awt.Point;

/**
 * Abstrakcyjna klasa reprezentująca kalkulator trasy dla danego koloru.
 * Implementacje zwracają współrzędne (kolumna, wiersz) dla danego countera.
 */
public abstract class RouteCalculator {
    public abstract Point getPosition(int counter);
}
