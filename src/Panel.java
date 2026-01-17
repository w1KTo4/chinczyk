import board.Board;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import model.Piece;
import model.PieceColor;

public class Panel extends JPanel implements MouseListener {

    private final int ROZMIAR_POLA = 55;
    private final int ILOSC_POL = 15;

    private Board board;
    private int liczba = 0; // wynik kostki

    public Panel() {
        addMouseListener(this);
        this.setPreferredSize(new Dimension(ROZMIAR_POLA * ILOSC_POL, ROZMIAR_POLA * ILOSC_POL));
        this.setBackground(Color.WHITE);

        board = new Board(ILOSC_POL);
        board.initializeStartPositions();
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

        // 1) klik środek -> losuj kostkę
        if (wiersz == 7 && kolumn == 7) {
            liczba = (int) (Math.random() * 6) + 1; // 1..6
            repaint();
            return;
        }

        // 2) jeśli na polu nie ma pionka -> nic
        Piece p = board.getPieceAt(wiersz, kolumn);
        if (p == null) return;

        PieceColor c = p.getColor();
        if (c == null) return;

        // 3) PRZENIESIENIE Z BAZY NA POLE STARTOWE (robimy to NAJPIERW)
        if (board.tryMoveFromBase(wiersz, kolumn)) {
            repaint();
            return;
        }

        // dla pionków counter-based: oblicz nowy counter i spróbuj przesunąć
        if (c == PieceColor.GREEN || c == PieceColor.BLUE || c == PieceColor.YELLOW || c == PieceColor.RED) {
            // jeśli czerwony musi najpierw mieć rzut
            if (c == PieceColor.RED && liczba <= 0) return;

            int oldCounter = p.getCounter();
            int nowyCounter = oldCounter + liczba;
            if (nowyCounter >= 56) nowyCounter = 55;
            Point cel = board.getPositionForColorAndCounter(c, nowyCounter);

            if (board.isOccupied(cel.y, cel.x)) {
                // pole zajęte -> anuluj ruch (zgodnie z oryginalną logiką)
                return;
            }

            // wykonaj ruch
            board.movePiece(wiersz, kolumn, cel.y, cel.x, nowyCounter);
            liczba = 0;
            repaint();
            return;
        }
    }

    // ---------------------------
    // Puste implementacje MouseListener
    // ---------------------------
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

        // rysuj planszę (Board przygotowuje mapę kolorów)
        board.drawBoard(g, ROZMIAR_POLA);

        // rysuj pionki
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                Piece piece = board.getPieceAt(w, k);
                if (piece != null) {
                    rysujPionek(g, k, w, piece.getAWTColor(), ROZMIAR_POLA);
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

    private void rysujPionek(Graphics g, int x, int y, Color kolor, int size) {
        g.setColor(kolor);
        g.fillOval(x * size + 5, y * size + 5, size - 10, size - 10);
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
}
