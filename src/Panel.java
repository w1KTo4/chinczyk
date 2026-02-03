import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import board.Board;
import model.Piece;
import model.PieceColor;
public class Panel extends JPanel implements MouseListener {
    private final int ROZMIAR_POLA = 40;
    private final int ILOSC_POL = 15;
    private Board board;
    private int liczba = 0;
    public Panel() {
        addMouseListener(this);
        this.setPreferredSize(new Dimension(ROZMIAR_POLA * ILOSC_POL, ROZMIAR_POLA * ILOSC_POL));
        this.setBackground(Color.WHITE);
        this.board = new Board(ILOSC_POL);
        this.board.initializeStartPositions();
    }
    // ---------------------------
    // Eventy myszy
    // ---------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        int kolumn = e.getX() / ROZMIAR_POLA;
        int wiersz = e.getY() / ROZMIAR_POLA;
        if (kolumn < 0 || kolumn >= ILOSC_POL || wiersz < 0 || wiersz >= ILOSC_POL) {
            return;
        }

        if (wiersz == 7 && kolumn == 7) {
            liczba = (int) (Math.random() * 6) + 1; // 1..6
            repaint();
            return;
        }

        Piece p = board.getPieceAt(wiersz, kolumn);
        if (p == null) return;
        PieceColor c = p.getColor();
        if (c == null) return;

        if (board.tryMoveFromBase(wiersz, kolumn)) {
            repaint();
            return;
        }

        if (c == PieceColor.RED && liczba <= 0) return;
        int oldCounter = p.getCounter();
        int nowyCounter = oldCounter + liczba;
        if (nowyCounter >= 56) nowyCounter = 55;
        Point cel = board.getPositionForColorAndCounter(c, nowyCounter);

        boolean moved = board.attemptMove(wiersz, kolumn, cel.y, cel.x, nowyCounter);
        if (!moved) {

            return;
        }

        liczba = 0;
        repaint();
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    // ---------------------------
    // Rysowanie
    // ---------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        board.drawBoard(g, ROZMIAR_POLA);

        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                Piece piece = board.getPieceAt(w, k);
                if (piece != null) {
                    rysujPionek(g, k, w, piece.getAWTColor());
                }
            }
        }

        if (liczba > 0) {
            rysujLiczba(
                    g,
                    7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
                    7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
                    liczba
            );
        }
    }
    private void rysujPionek(Graphics g, int x, int y, Color kolor) {
        g.setColor(kolor);
        g.fillOval(x * ROZMIAR_POLA + 5, y * ROZMIAR_POLA + 5, ROZMIAR_POLA - 10, ROZMIAR_POLA - 10);
    }
    private void rysujLiczba(Graphics g, int x, int y, int liczba) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        String tekst = String.valueOf(liczba);
        int tekstX = x - fm.stringWidth(tekst) / 2;
        int tekstY = y + fm.getAscent() / 2;
        g.drawString(tekst, tekstX, tekstY);
    }

    public void restartGame() {
        if (this.board != null) {
            board.initializeStartPositions();
        }
        this.liczba = 0;
        repaint();
    }
}