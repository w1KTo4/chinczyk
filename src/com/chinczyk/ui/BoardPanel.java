package com.chinczyk.ui;

import com.chinczyk.logic.Game;
import com.chinczyk.model.Piece;
import com.chinczyk.model.Tile;
import com.chinczyk.model.ColorType;
import com.chinczyk.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel rysujący planszę i pionki.
 * Poprawione:
 * - kolory domów (zielony/niebieski nie są już zamienione),
 * - pola wejścia do domów i trasy zgodnie z Twoimi korektami,
 * - kliknięcia wykonują ruch tylko jeśli kostka została rzucona i ruch jest możliwy,
 * - wskaźnik tury rysowany tylko na dole (prawy boczny label usunięty).
 */
public class BoardPanel extends JPanel {
    private Game game;
    private int grid = 15; // A1–O15
    private int tileSize = 40;

    public BoardPanel(Game game) {
        this.game = game;
        this.setBackground(Color.WHITE);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int mouseX, int mouseY) {
        int col = mouseX / tileSize + 1; // 1-based
        int row = mouseY / tileSize + 1; // 1-based

        Player current = game.getCurrentPlayer();

        // kliknięcia mają sens tylko jeśli gracz jest human i kostka została rzucona
        if (!(current instanceof com.chinczyk.model.HumanPlayer)) {
            System.out.println("[DEBUG] Click ignored: not human player's turn.");
            return;
        }
        if (!game.hasRolledThisTurn()) {
            System.out.println("[DEBUG] Click ignored: kostka nie została rzucona.");
            return;
        }

        int dice = game.getLastRollValue();

        // Najpierw sprawdź pola trasy: czy kliknięto na pionek gracza
        for (Tile t : game.getTiles()) {
            if (t.getRow() == row && t.getCol() == col && !t.isEmpty()) {
                Piece p = t.getOccupant();
                if (p.getColor() == current.getColor()) {
                    if (game.canMovePiece(current, p, dice)) {
                        boolean moved = game.movePiece(current, p, dice);
                        game.placeAllPiecesOnTiles();
                        repaint();
                        System.out.println("[DEBUG] " + current.getName() + " moved piece " + p.getId() + " by clicking on board.");
                        // po ruchu sprawdź czy zachowuje turę (6)
                        game.afterMoveAdvanceIfNeeded(moved);
                        return;
                    } else {
                        System.out.println("[DEBUG] Move not allowed for piece " + p.getId());
                        return;
                    }
                }
            }
        }

        // jeśli kliknięto w dom (pola startowe), sprawdź czy tam jest pionek gracza
        for (Piece p : current.getPieces()) {
            if (p.getState() == com.chinczyk.model.PieceState.HOME) {
                int[] slot = game.getHomeSlotForPiece(p);
                if (slot[0] == row && slot[1] == col) {
                    if (dice == 6 && game.canMovePiece(current, p, dice)) {
                        boolean moved = game.movePiece(current, p, dice);
                        game.placeAllPiecesOnTiles();
                        repaint();
                        System.out.println("[DEBUG] " + current.getName() + " moved piece " + p.getId() + " from HOME by clicking home slot.");
                        game.afterMoveAdvanceIfNeeded(moved);
                        return;
                    } else {
                        System.out.println("[DEBUG] Clicked home slot but cannot move (need 6).");
                        return;
                    }
                }
            }
        }

        // jeśli nic nie zadziałało, nic nie robimy (nie wykonujemy losowych ruchów)
        System.out.println("[DEBUG] Click on empty/irrelevant field - no action.");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        tileSize = Math.min(getWidth(), getHeight()) / grid;
        drawBoard(g);
        drawPieces(g);
        drawDice(g);
        drawCurrentPlayerIndicator(g);
    }

    private void drawBoard(Graphics g) {
        int size = tileSize;

        // tło
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // rysujemy pola 15x15 zgodnie z poprawkami:
        // - domy: czerwony B2–E5, zielony K2–N5, niebieski B11–E14, żółty K11–N14
        // - pola startowe 2x2 w środku domów (kolory zgodne)
        // - pola wejścia do domów (wg Twojego opisu):
        //   niebieskie wejście: B8–E8 (cols 2..5,row 8)
        //   czerwone wejście: H2–H5 (col 8, rows 2..5)
        //   zielone wejście: N8–K8 (cols 14..11,row 8)  (uwaga: rysujemy od K do N)
        //   żółte wejście: H14–H11 (col 8, rows 14..11)
        // - trasy korytarzy: A9-G9, A7-G7, I9-O9, I7-O7 (poprawione rysowanie)

        for (int row = 1; row <= grid; row++) {
            for (int col = 1; col <= grid; col++) {
                int x = (col - 1) * size;
                int y = (row - 1) * size;

                Color fill = Color.WHITE;

                // DOMY 4x4 i pola startowe 2x2
                if (row >= 2 && row <= 5 && col >= 2 && col <= 5) fill = new Color(255, 180, 180); // czerwony dom
                if (row >= 3 && row <= 4 && col >= 3 && col <= 4) fill = Color.RED; // czerwone pola startowe

                if (row >= 2 && row <= 5 && col >= 11 && col <= 14) fill = new Color(180, 255, 180); // zielony dom
                if (row >= 3 && row <= 4 && col >= 12 && col <= 13) fill = Color.GREEN; // zielone pola startowe

                if (row >= 11 && row <= 14 && col >= 2 && col <= 5) fill = new Color(180, 180, 255); // niebieski dom
                if (row >= 12 && row <= 13 && col >= 3 && col <= 4) fill = Color.BLUE; // niebieskie pola startowe

                if (row >= 11 && row <= 14 && col >= 11 && col <= 14) fill = new Color(255, 255, 180); // żółty dom
                if (row >= 12 && row <= 13 && col >= 12 && col <= 13) fill = Color.YELLOW; // żółte pola startowe

                // WEJŚCIA DO DOMÓW (wg Twojego opisu)
                // niebieskie wejście: B8–E8 (cols 2..5,row 8)
                if (row == 8 && col >= 2 && col <= 5) fill = Color.LIGHT_GRAY;
                // czerwone wejście: H2–H5 (col 8, rows 2..5)
                if (col == 8 && row >= 2 && row <= 5) fill = new Color(255, 150, 150);
                // zielone wejście: N8–K8 (cols 14..11,row 8) -> rysujemy jako ciąg cols 11..14
                if (row == 8 && col >= 11 && col <= 14) fill = Color.LIGHT_GRAY;
                // żółte wejście: H14–H11 (col 8, rows 11..14)
                if (col == 8 && row >= 11 && row <= 14) fill = new Color(255, 255, 150);

                // TRASA: rysujemy korytarze i obwód
                // A9-G9 (cols 1..7,row 9)
                if (row == 9 && col >= 1 && col <= 7) fill = Color.LIGHT_GRAY;
                // A7-G7 (cols 1..7,row 7)
                if (row == 7 && col >= 1 && col <= 7) fill = Color.LIGHT_GRAY;
                // I9-O9 (cols 9..15,row 9)
                if (row == 9 && col >= 9 && col <= 15) fill = Color.LIGHT_GRAY;
                // I7-O7 (cols 9..15,row 7)
                if (row == 7 && col >= 9 && col <= 15) fill = Color.LIGHT_GRAY;

                // dodatkowe elementy: centralne korytarze i pola startowe na rogach
                // górna część: G1-H1 trasa, I1 czerwony start
                if (row == 1 && (col == 7 || col == 8)) fill = Color.LIGHT_GRAY;
                if (row == 1 && col == 9) fill = Color.RED;

                // rzędy 2–6: Gx trasa, Hx czerwony cel, Ix trasa (zachowujemy)
                if (row >= 2 && row <= 6) {
                    if (col == 7 || col == 9) fill = Color.LIGHT_GRAY;
                    if (col == 8) fill = new Color(255, 150, 150);
                }

                // środek (kolory pól meta)
                if (row == 7 || row == 8) {
                    if (col >= 3 && col <= 5) fill = new Color(150, 255, 150); // zielony pole meta
                    if (col >= 11 && col <= 13) fill = new Color(150, 150, 255); // niebieskie pole meta
                    if (col >= 7 && col <= 9) fill = Color.WHITE; // środek
                }
                if (row == 9) {
                    if (col >= 3 && col <= 5) fill = new Color(150, 255, 150);
                    if (col >= 11 && col <= 13) fill = new Color(150, 150, 255);
                    if (col >= 7 && col <= 9) fill = Color.WHITE;
                    // starty: zielony O9 (col 15,row9) i niebieski A7 (col1,row7) są w path, ale tutaj rysujemy starty
                    if (col == 15 && row == 9) fill = Color.GREEN;
                    if (col == 1 && row == 7) fill = Color.BLUE;
                }

                // rysuj pole
                g.setColor(fill);
                g.fillRect(x, y, size, size);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, size, size);
            }
        }
    }

    private void drawPieces(Graphics g) {
        int size = tileSize;
        int padding = Math.max(4, size / 8);

        // Rysujemy pionki na trasie
        for (Tile t : game.getTiles()) {
            if (!t.isEmpty()) {
                Piece p = t.getOccupant();
                int col = t.getCol() - 1;
                int row = t.getRow() - 1;
                int x = col * size;
                int y = row * size;

                g.setColor(convertColor(p.getColor()));
                g.fillOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
                g.setColor(Color.BLACK);
                g.drawOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
            }
        }

        // Rysujemy pionki w domach (HOME)
        for (Player player : game.getPlayers()) {
            for (Piece p : player.getPieces()) {
                if (p.getState() == com.chinczyk.model.PieceState.HOME) {
                    int[] slot = game.getHomeSlotForPiece(p); // {row,col}
                    int col = slot[1] - 1;
                    int row = slot[0] - 1;
                    int x = col * size;
                    int y = row * size;

                    g.setColor(convertColor(p.getColor()));
                    g.fillOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
                    g.setColor(Color.BLACK);
                    g.drawOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
                }
            }
        }
    }

    private Color convertColor(ColorType ct) {
        switch (ct) {
            case RED: return Color.RED;
            case GREEN: return Color.GREEN;
            case BLUE: return Color.BLUE;
            case YELLOW: return Color.YELLOW;
            default: return Color.GRAY;
        }
    }

    private void drawDice(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        int size = tileSize * 2;
        int x = w / 2 - size / 2;
        int y = h / 2 - size / 2;

        g.setColor(Color.WHITE);
        g.fillRect(x, y, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, size, size);

        int val = game.getLastRollValue();
        g.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
        String s = val == 0 ? "-" : String.valueOf(val);
        FontMetrics fm = g.getFontMetrics();
        int sw = fm.stringWidth(s);
        int sh = fm.getAscent();
        g.drawString(s, x + (size - sw) / 2, y + (size + sh) / 2 - 4);
    }

    private void drawCurrentPlayerIndicator(Graphics g) {
        String text = "Tura: " + game.getCurrentPlayerName();
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int x = getWidth() - textWidth - 10;
        int y = getHeight() - 6;

        Color bg = new Color(255, 255, 255, 200);
        g.setColor(bg);
        g.fillRect(x - 6, y - textHeight, textWidth + 12, textHeight + 6);

        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
    }
}
