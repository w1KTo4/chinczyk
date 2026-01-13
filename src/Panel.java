import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

public class Panel extends JPanel implements MouseListener{
    private final int ROZMIAR_POLA = 55;
    private final int ILOSC_POL = 15;
    Color[][] mapa = new Color[ILOSC_POL][ILOSC_POL];
    boolean[][] pionek = new boolean[ILOSC_POL][ILOSC_POL];
    Color[][] pionekKolor = new Color[ILOSC_POL][ILOSC_POL];
    int[][] pionekCounter = new int[ILOSC_POL][ILOSC_POL]; // numer pola na torze dla czerwonego
    int liczba;

    public Panel()
    {
        addMouseListener(this);
        this.setPreferredSize(new Dimension(ROZMIAR_POLA*ILOSC_POL, ROZMIAR_POLA*ILOSC_POL));
        this.setBackground(Color.WHITE);

        int[][] pozycje = {
                {2, 2}, {3, 2}, {2, 3}, {3, 3},
                {11, 2}, {11, 3}, {12, 2}, {12, 3},
                {2, 11}, {3, 11}, {2, 12}, {3, 12},
                {11, 11}, {12, 11}, {11, 12}, {12, 12}
        };
        int counter=0;
        for (int[] p : pozycje) {
            pionek[p[1]][p[0]] = true;
            if (counter >= 0 && counter < 4) {
                pionekKolor[p[1]][p[0]] = new Color(153, 0, 0); // czerwony
                pionekCounter[p[1]][p[0]] = 0; // start counter
            } else if (counter >= 4 && counter < 8) {
                pionekKolor[p[1]][p[0]] = new Color(0, 153, 0); // zielony
            } else if (counter >= 8 && counter < 12) {
                pionekKolor[p[1]][p[0]] = new Color(0, 0, 153); // niebieski
            } else if (counter >= 12 && counter < 16) {
                pionekKolor[p[1]][p[0]] = new Color(204, 204, 0); // żółty
            }
            counter++;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int kolumn = e.getX() / ROZMIAR_POLA;
        int wiersz  = e.getY() / ROZMIAR_POLA;

        if (kolumn < 0 || kolumn >= ILOSC_POL || wiersz < 0 || wiersz >= ILOSC_POL) return;

        // 1) klik środek -> losuj kostkę
        if (wiersz == 7 && kolumn == 7) {
            liczba = (int)(Math.random() * 6) + 1; // 1..6
            repaint();
            return;
        }

        // 2) jeśli na polu nie ma pionka -> nic
        if (!pionek[wiersz][kolumn]) return;

        Color c = pionekKolor[wiersz][kolumn];
        if (c == null) return;

        // 3) PRZENIESIENIE Z BAZY NA POLE STARTOWE (robimy to NAJPIERW)
        if (c.equals(new Color(153,0,0)) && wiersz >= 1 && wiersz <= 4 && kolumn >= 1 && kolumn <= 4) {
            int sr = 0, sc = 8;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                pionekCounter[sr][sc] = 0;
                repaint(); return;
            } else return;
        }

        if (c.equals(new Color(0,153,0)) && wiersz >= 1 && wiersz <= 4 && kolumn >= 10 && kolumn <= 13) {
            int sr = 8, sc = 14;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        if (c.equals(new Color(0,0,153)) && wiersz >= 10 && wiersz <= 13 && kolumn >= 1 && kolumn <= 4) {
            int sr = 6, sc = 0;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        if (c.equals(new Color(204,204,0)) && wiersz >= 10 && wiersz <= 13 && kolumn >= 10 && kolumn <= 13) {
            int sr = 14, sc = 6;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        // 4) RUCH PIONKA - tylko czerwony z counterem
        if (liczba <= 0) return; // trzeba najpierw rzucić

        if (c.equals(new Color(153,0,0))) { // czerwony
            int oldCounter = pionekCounter[wiersz][kolumn];
            int nowyCounter = oldCounter + liczba;
            if (nowyCounter >= 56) nowyCounter = 55;
            Point p = getCzerwonyPoCounterze(nowyCounter);

            if (pionek[p.y][p.x]) return; // jeśli pole zajęte -> anuluj ruch

            // wykonaj ruch
            pionek[wiersz][kolumn] = false;
            pionekKolor[wiersz][kolumn] = null;

            pionek[p.y][p.x] = true;
            pionekKolor[p.y][p.x] = c;
            pionekCounter[p.y][p.x] = nowyCounter;

            liczba = 0; // zużyj rzut
            repaint();
            return;
        }

        // dla innych kolorów: prosty ruch (jak wcześniej)
        int nowyWiersz = wiersz;
        int nowaKolumna = kolumn;
        if (c.equals(new Color(0,153,0))) nowaKolumna = kolumn - liczba;
        else if (c.equals(new Color(0,0,153))) nowaKolumna = kolumn + liczba;
        else if (c.equals(new Color(204,204,0))) nowyWiersz = wiersz - liczba;

        if (nowyWiersz < 0) nowyWiersz = 0;
        if (nowyWiersz >= ILOSC_POL) nowyWiersz = ILOSC_POL - 1;
        if (nowaKolumna < 0) nowaKolumna = 0;
        if (nowaKolumna >= ILOSC_POL) nowaKolumna = ILOSC_POL - 1;

        if (pionek[nowyWiersz][nowaKolumna]) return;

        pionek[wiersz][kolumn] = false;
        pionekKolor[wiersz][kolumn] = null;
        pionek[nowyWiersz][nowaKolumna] = true;
        pionekKolor[nowyWiersz][nowaKolumna] = c;

        liczba = 0;
        repaint();
    }

    private Point getCzerwonyPoCounterze(int counter) {
        int w = 0, k = 8; // start: górny środek

        if (counter <= 6) {            // w dół 6 pól
            w = counter;
            k = 8;
        } else if (counter < 12) {    // w prawo 6 pól
            w = 6;
            k = 8 + (counter - 6) ; // pola: 8..13
        } else if (counter < 14) {    // w dół 2 pola
            w = 6 + (counter - 12) + 1; // 7..8
            k = 14;
        } else if (counter < 20) {    // w lewo 6 pól
            w = 8;
            k = 13 - (counter - 14) - 1; // 12..7
        } else if (counter < 26) {    // w dół 6 pól
            w = 9 + (counter - 20);   // 9..14
            k = 7;
        } else if (counter < 28) {    // w lewo 2 pola
            w = 14;
            k = 6 - (counter - 26);   // 6..5
        } else if (counter < 34) {    // w górę 6 pól
            w = 14 - (counter - 28);  // 14..9
            k = 5;
        } else if (counter < 40) {    // w lewo 6 pól
            w = 8;
            k = 4 - (counter - 34);   // 4..-1 → ograniczamy do 0
            if (k < 0) k = 0;
        } else if (counter < 42) {    // w górę 2 pola
            w = 7 - (counter - 40);   // 7..6
            k = 0;
        } else if (counter < 48) {    // w prawo 6 pól
            w = 6;
            k = 1 + (counter - 42);   // 1..6
        } else if (counter < 54) {    // w górę 6 pól
            w = 5 - (counter - 48);   // 5..0
            k = 7;
        } else {                       // w prawo 2 pola do pola startowego
            w = 0;
            k = 8 + (counter - 54);    // 8..9 (kończymy na 9?)
        }

        // ograniczenie do planszy
        if (w < 0) w = 0;
        if (w >= ILOSC_POL) w = ILOSC_POL - 1;
        if (k < 0) k = 0;
        if (k >= ILOSC_POL) k = ILOSC_POL - 1;

        return new Point(k, w);
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        rysujPlansze(g);
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                if (pionek[w][k]) {
                    rysujPionek(g, k, w, pionekKolor[w][k]);
                }
            }
        }
        if (liczba > 0) {
            rysujLiczba(g,
                    7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
                    7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
                    liczba);
        }
    }

    private void rysujPlansze(Graphics g)
    {
        for (int w = 0; w < ILOSC_POL; w++)
            for (int k = 0; k < ILOSC_POL; k++) mapa[w][k] = null;

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

        for (int w = 0; w < ILOSC_POL; w++)
            for (int k = 0; k < ILOSC_POL; k++) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(k*ROZMIAR_POLA, w*ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
            }

        for (int w = 0; w < ILOSC_POL; w++)
            for (int k = 0; k < ILOSC_POL; k++) {
                if (mapa[w][k] != null) {
                    g.setColor(mapa[w][k]);
                    g.fillRect(k*ROZMIAR_POLA, w*ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
                }
            }

        g.setColor(Color.RED);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA*4, ROZMIAR_POLA*4);
        g.setColor(Color.GREEN);
        g.fillRect(ROZMIAR_POLA*10, ROZMIAR_POLA, ROZMIAR_POLA*4, ROZMIAR_POLA*4);
        g.setColor(Color.YELLOW);
        g.fillRect(ROZMIAR_POLA*10, ROZMIAR_POLA*10, ROZMIAR_POLA*4, ROZMIAR_POLA*4);
        g.setColor(Color.BLUE);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA*10, ROZMIAR_POLA*4, ROZMIAR_POLA*4);

        g.setColor(Color.BLACK);
        for (int w = 0; w < ILOSC_POL; w++)
            for (int k = 0; k < ILOSC_POL; k++)
                g.drawRect(k*ROZMIAR_POLA, w*ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
    }

    private void rysujPionek(Graphics g, int x, int y, Color kolor)
    {
        g.setColor(kolor);
        g.fillOval(x*ROZMIAR_POLA+5, y*ROZMIAR_POLA+5, ROZMIAR_POLA-10, ROZMIAR_POLA-10);
    }

    private void rysujLiczba(Graphics g, int x, int y, int liczba)
    {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        String tekst = String.valueOf(liczba);
        int tekstX = x - fm.stringWidth(tekst)/2;
        int tekstY = y + fm.getAscent()/2;
        g.drawString(tekst, tekstX, tekstY);
    }
}
