import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;


public class Panel extends JPanel implements MouseListener{
    private final int ROZMIAR_POLA = 55;
    private final int ILOSC_POL = 15;
    Color[][] mapa = new Color[15][15];
    boolean[][] pionek = new boolean[ILOSC_POL][ILOSC_POL];
    Color[][] pionekKolor = new Color[ILOSC_POL][ILOSC_POL];
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
    pionekKolor[p[1]][p[0]] = new Color(153, 0, 0);
} else if (counter >= 4 && counter < 8) {
    pionekKolor[p[1]][p[0]] = new Color(0, 153, 0);
} else if (counter >= 8 && counter < 12) {
    pionekKolor[p[1]][p[0]] = new Color(0, 0, 153);
} else if (counter >= 12 && counter < 16) {
    pionekKolor[p[1]][p[0]] = new Color(204, 204, 0);
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
        // czerwony baza (1..4,1..4) -> start (0,8)
        if (c.equals(new Color(153,0,0)) && wiersz >= 1 && wiersz <= 4 && kolumn >= 1 && kolumn <= 4) {
            int sr = 0, sc = 8;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        // zielony baza (1..4,10..13) -> start (8,14)
        if (c.equals(new Color(0,153,0)) && wiersz >= 1 && wiersz <= 4 && kolumn >= 10 && kolumn <= 13) {
            int sr = 8, sc = 14;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        // niebieski baza (10..13,1..4) -> start (6,0)
        if (c.equals(new Color(0,0,153)) && wiersz >= 10 && wiersz <= 13 && kolumn >= 1 && kolumn <= 4) {
            int sr = 6, sc = 0;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        // żółty baza (10..13,10..13) -> start (14,6)
        if (c.equals(new Color(204,204,0)) && wiersz >= 10 && wiersz <= 13 && kolumn >= 10 && kolumn <= 13) {
            int sr = 14, sc = 6;
            if (!pionek[sr][sc]) {
                pionek[wiersz][kolumn] = false; pionekKolor[wiersz][kolumn] = null;
                pionek[sr][sc] = true; pionekKolor[sr][sc] = c;
                repaint(); return;
            } else return;
        }

        // 4) JEŚLI NIE BYŁO PRZENIESIENIA Z BAZY -> ruch w dół o "liczba" (jeśli liczba>0)
        if (liczba <= 0) return; // trzeba najpierw rzucić

        int nowyWiersz = wiersz + liczba;
        if (nowyWiersz >= ILOSC_POL) nowyWiersz = ILOSC_POL - 1;

        // zabezpieczenie: jeśli docelowe pole zajęte -> anuluj ruch
        if (pionek[nowyWiersz][kolumn]) return;

        // wykonaj ruch
        pionek[wiersz][kolumn] = false;
        pionekKolor[wiersz][kolumn] = null;
        pionek[nowyWiersz][kolumn] = true;
        pionekKolor[nowyWiersz][kolumn] = c;

        liczba = 0; // zużyj rzut
        repaint();
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
    if (liczba > 0) {
        rysujLiczba(g,
            7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
            7 * ROZMIAR_POLA + ROZMIAR_POLA / 2,
            liczba);
    }
}
    }

    private void rysujPlansze(Graphics g)
    {
        // 0) wyczyść mapę
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                mapa[w][k] = null;
            }
        }

        // 1) ustawiamy pola toru w tablicy mapa (logika)
        for (int i = 1; i < 8; i++) {
            mapa[i][7] = Color.RED;
            mapa[i-1][7-1] = Color.GRAY;
            mapa[i-1][7+1] = Color.GRAY;
        }
        for (int i = 14; i > 7; i--) {
            mapa[7][i-1] = Color.GREEN;
            mapa[7-1][i] = Color.GRAY;
            mapa[7+1][i] = Color.GRAY;
        }
        for (int i = 1; i < 8; i++) {
            mapa[7][i] = Color.BLUE;
            mapa[7-1][i-1] = Color.GRAY;
            mapa[7+1][i-1] = Color.GRAY;
        }
        for (int i = 14; i > 7; i--) {
            mapa[i-1][7] = Color.YELLOW;
            mapa[i][7-1] = Color.GRAY;
            mapa[i][7+1] = Color.GRAY;
        }
        mapa[0][7] = Color.GRAY;
        mapa[7][0] = Color.GRAY;
        mapa[7][14] = Color.GRAY;
        mapa[14][7] = Color.GRAY;
        mapa[7][7] = Color.WHITE; // pole środkowe

        // 2) tło pól (siatka)
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
            }
        }

        // 3) rysuj pola kolorowe z mapy (tor)
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                if (mapa[w][k] != null) {
                    g.setColor(mapa[w][k]);
                    g.fillRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
                }
            }
        }

        // 4) duże pola startowe (bazy) narysuj na wierzchu, żeby były widoczne
        g.setColor(Color.RED);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4);         // lewy górny

        g.setColor(Color.GREEN);
        g.fillRect(ROZMIAR_POLA * 10, ROZMIAR_POLA, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4);    // prawy górny

        g.setColor(Color.YELLOW);
        g.fillRect(ROZMIAR_POLA * 10, ROZMIAR_POLA * 10, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4); // prawy dolny

        g.setColor(Color.BLUE);
        g.fillRect(ROZMIAR_POLA, ROZMIAR_POLA * 10, ROZMIAR_POLA * 4, ROZMIAR_POLA * 4);    // lewy dolny

        // 5) kontury pól na sam koniec
        g.setColor(Color.BLACK);
        for (int w = 0; w < ILOSC_POL; w++) {
            for (int k = 0; k < ILOSC_POL; k++) {
                g.drawRect(k * ROZMIAR_POLA, w * ROZMIAR_POLA, ROZMIAR_POLA, ROZMIAR_POLA);
            }
        }
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
    int tekstX = x - fm.stringWidth(tekst) / 2;
    int tekstY = y + fm.getAscent() / 2;
    g.drawString(tekst, tekstX, tekstY);
}

}
