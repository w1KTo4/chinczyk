package board;
import java.awt.*;
import model.Piece;
import model.PieceColor;
import route.BlueRouteCalculator;
import route.GreenRouteCalculator;
import route.RedRouteCalculator;
import route.RouteCalculator;
import route.YellowRouteCalculator;

public class Board {
    private final int size;
    private final Color[][] mapa;
    private final Piece[][] pieces;

    private final RouteCalculator redRoute = new RedRouteCalculator();
    private final RouteCalculator greenRoute = new GreenRouteCalculator();
    private final RouteCalculator blueRoute = new BlueRouteCalculator();
    private final RouteCalculator yellowRoute = new YellowRouteCalculator();

    public Board(int size) {
        this.size = size;
        mapa = new Color[size][size];
        pieces = new Piece[size][size];
    }

    public void initializeStartPositions() {

    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
              pieces[i][j] = null; 
            
        }
    }


    int[][] pozycje = {
        {2, 2}, {3, 2}, {2, 3}, {3, 3},       // czerwone
        {11, 2}, {11, 3}, {12, 2}, {12, 3},   // zielone
        {2, 11}, {3, 11}, {2, 12}, {3, 12},   // niebieskie
        {11, 11}, {12, 11}, {11, 12}, {12, 12} // żółte
    };

    for (int counter = 0; counter < pozycje.length; counter++) {
        int row = pozycje[counter][1];
        int col = pozycje[counter][0];
        PieceColor color;

        if (counter < 4) color = PieceColor.RED;
        else if (counter < 8) color = PieceColor.GREEN;
        else if (counter < 12) color = PieceColor.BLUE;
        else color = PieceColor.YELLOW;

        Piece piece = new Piece(color, 0); 
        setPieceAt(row, col, piece);
    }
}



    public Piece getPieceAt(int row, int col) {
        if (valid(row, col)) return pieces[row][col];
        return null;
    }

    public boolean isOccupied(int row, int col) {
        if (!valid(row, col)) return true;
        return pieces[row][col] != null;
    }

    public void setPieceAt(int row, int col, Piece p) {
        if (valid(row, col)) pieces[row][col] = p;
    }

    public void removePieceAt(int row, int col) {
        if (valid(row, col)) pieces[row][col] = null;
    }

    private boolean valid(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    public boolean tryMoveFromBase(int row, int col) {
        Piece p = getPieceAt(row, col);
        if (p == null) return false;
        PieceColor c = p.getColor();

        // czerwony
        if (c == PieceColor.RED && row >= 1 && row <= 4 && col >= 1 && col <= 4) {
            int sr = 0, sc = 8;
            if (!isOccupied(sr, sc)) {
                removePieceAt(row, col);
                p.setCounter(0);
                setPieceAt(sr, sc, p);
                return true;
            } else return false;
        }

        // zielony
        if (c == PieceColor.GREEN && row >= 1 && row <= 4 && col >= 10 && col <= 13) {
            int sr = 8, sc = 14;
            if (!isOccupied(sr, sc)) {
                removePieceAt(row, col);
                p.setCounter(0);
                setPieceAt(sr, sc, p);
                return true;
            } else return false;
        }

        // niebieski
        if (c == PieceColor.BLUE && row >= 10 && row <= 13 && col >= 1 && col <= 4) {
            int sr = 6, sc = 0;
            if (!isOccupied(sr, sc)) {
                removePieceAt(row, col);
                p.setCounter(0);
                setPieceAt(sr, sc, p);
                return true;
            } else return false;
        }

        // zolty
        if (c == PieceColor.YELLOW && row >= 10 && row <= 13 && col >= 10 && col <= 13) {
            int sr = 14, sc = 6;
            if (!isOccupied(sr, sc)) {
                removePieceAt(row, col);
                p.setCounter(0);
                setPieceAt(sr, sc, p);
                return true;
            } else return false;
        }

        return false;
    }


    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, int newCounter) {
        Piece p = getPieceAt(fromRow, fromCol);
        if (p == null) return;
        removePieceAt(fromRow, fromCol);
        p.setCounter(newCounter);
        setPieceAt(toRow, toCol, p);
    }


    public Point getPositionForColorAndCounter(PieceColor color, int counter) {
        Point p;
        switch (color) {
            case RED:
                p = redRoute.getPosition(counter);
                break;
            case GREEN:
                p = greenRoute.getPosition(counter);
                break;
            case BLUE:
                p = blueRoute.getPosition(counter);
                break;
            case YELLOW:
                p = yellowRoute.getPosition(counter);
                break;
            default:
                p = redRoute.getPosition(counter);
        }

        int k = Math.max(0, Math.min(size - 1, p.x));
        int w = Math.max(0, Math.min(size - 1, p.y));
        return new Point(k, w);
    }


    public void drawBoard(Graphics g, int ROZMIAR_POLA) {
        // 0) wyczyść mapę
        for (int w = 0; w < size; w++) {
            for (int k = 0; k < size; k++) {
                mapa[w][k] = null;
            }
        }
        for (int i = 1; i < 8; i++) {
            mapa[i][7] = Color.RED;
            mapa[i-1][6] = Color.GRAY;
            mapa[i-1][8] = Color.GRAY;
        }
        for (int i = 14; i > 7; i--) {
            mapa[7][i-1] = Color.GREEN;
            mapa[6][i] = Color.GRAY;
            mapa[8][i] = Color.GRAY;
        }
        for (int i = 1; i < 8; i++) {
            mapa[7][i] = Color.BLUE;
            mapa[6][i-1] = Color.GRAY;
            mapa[8][i-1] = Color.GRAY;
        }
        for (int i = 14; i > 7; i--) {
            mapa[i-1][7] = Color.YELLOW;
            mapa[i][6] = Color.GRAY;
            mapa[i][8] = Color.GRAY;
        }

        mapa[0][7] = Color.GRAY;
        mapa[7][0] = Color.GRAY;
        mapa[7][14] = Color.GRAY;
        mapa[14][7] = Color.GRAY;
        mapa[7][7] = Color.WHITE; 


        for (int w = 0; w < size; w++) {
            for (int k = 0; k < size; k++) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
            }
        }

        for (int w = 0; w < size; w++) {
            for (int k = 0; k < size; k++) {
                if (mapa[w][k] != null) {
                    g.setColor(mapa[w][k]);
                    g.fillRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
                }
            }
        }


        g.setColor(Color.RED);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4);    

        g.setColor(Color.GREEN);
        g.fillRect(ROZMIAR_POLA * 10, ROZMIAR_POLA, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4); 

        g.setColor(Color.YELLOW);
        g.fillRect(ROZMIAR_POLA * 10, ROZMIAR_POLA * 10, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4); 

        g.setColor(Color.BLUE);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA * 10, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4);   
        
        g.setColor(Color.BLACK);
        for (int w = 0; w < size; w++) {
            for (int k = 0; k < size; k++) {
                g.drawRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
            }
        }
    }
}
