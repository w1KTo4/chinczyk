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
 * Plansza odwzorowana wg układu A1–O15 (15x15).
 * Pionki rysowane jako circle (okręgi).
 * Pokazuje w prawym dolnym rogu, który gracz ma turę (tekst rysowany na panelu).
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
        int dice = game.getDiceValue();

        // Najpierw sprawdź pola trasy
        for (Tile t : game.getTiles()) {
            if (t.getRow() == row && t.getCol() == col && !t.isEmpty()) {
                Piece p = t.getOccupant();
                if (p.getColor() == current.getColor()) {
                    if (game.canMovePiece(current, p, dice)) {
                        game.movePiece(current, p, dice);
                        game.placeAllPiecesOnTiles();
                        repaint();
                        game.nextTurn();
                        System.out.println("[DEBUG] " + current.getName() + " moved piece " + p.getId() + " by clicking on board.");
                        // jeśli następny jest AI, uruchamiamy AI tury
                        SwingUtilities.invokeLater(() -> {
                            try { Thread.sleep(50); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                            // wywołanie AI tury z GameFrame (tam jest pętla AI) - ale nie mamy referencji do GameFrame tutaj,
                            // więc wystarczy odświeżyć i pozwolić GameFrame.handleAITurnsIfNeeded zostać wywołanym po kliknięciu rolla lub w GameFrame.
                        });
                        return;
                    } else {
                        System.out.println("[DEBUG] Move not allowed for piece " + p.getId());
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
                        game.movePiece(current, p, dice);
                        game.placeAllPiecesOnTiles();
                        repaint();
                        game.nextTurn();
                        System.out.println("[DEBUG] " + current.getName() + " moved piece " + p.getId() + " from HOME by clicking home slot.");
                        return;
                    } else {
                        System.out.println("[DEBUG] Clicked home slot but cannot move (need 6).");
                    }
                }
            }
        }

        // jeśli nic nie zadziałało, spróbuj automatycznie poruszyć pierwszy możliwy pionek
        for (Piece p : current.getPieces()) {
            if (game.canMovePiece(current, p, dice)) {
                game.movePiece(current, p, dice);
                game.placeAllPiecesOnTiles();
                repaint();
                game.nextTurn();
                System.out.println("[DEBUG] " + current.getName() + " auto-moved piece " + p.getId());
                return;
            }
        }
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

        // rysujemy pola 15x15 zgodnie z opisem (A1..O15)
        for (int row = 1; row <= grid; row++) {
            for (int col = 1; col <= grid; col++) {
                int x = (col - 1) * size;
                int y = (row - 1) * size;

                Color fill = Color.WHITE;

                // DOMY 4x4 i pola startowe 2x2 (zgodnie z opisem)
                // Czerwony dom B2–E5 (cols 2..5, rows 2..5), start C3-D4
                if (row >= 2 && row <= 5 && col >= 2 && col <= 5) fill = new Color(255, 180, 180);
                if (row >= 3 && row <= 4 && col >= 3 && col <= 4) fill = Color.RED;

                // Zielony dom K2–N5 (cols 11..14, rows 2..5), start L3-M4
                if (row >= 2 && row <= 5 && col >= 11 && col <= 14) fill = new Color(180, 255, 180);
                if (row >= 3 && row <= 4 && col >= 12 && col <= 13) fill = Color.GREEN;

                // Niebieski dom B11–E14 (cols 2..5, rows 11..14), start C12-D13
                if (row >= 11 && row <= 14 && col >= 2 && col <= 5) fill = new Color(180, 180, 255);
                if (row >= 12 && row <= 13 && col >= 3 && col <= 4) fill = Color.BLUE;

                // Żółty dom K11–N14 (cols 11..14, rows 11..14), start L12-M13
                if (row >= 11 && row <= 14 && col >= 11 && col <= 14) fill = new Color(255, 255, 180);
                if (row >= 12 && row <= 13 && col >= 12 && col <= 13) fill = Color.YELLOW;

                // TRASA i CELE (zgodnie z wcześniejszym opisem)
                // Rząd 1: G1–H1 trasa, I1 start czerwony
                if (row == 1 && (col == 7 || col == 8)) fill = Color.LIGHT_GRAY;
                if (row == 1 && col == 9) fill = Color.RED;

                // Rzędy 2–6: Gx trasa, Hx czerwony cel, Ix trasa
                if (row >= 2 && row <= 6) {
                    if (col == 7 || col == 9) fill = Color.LIGHT_GRAY;
                    if (col == 8) fill = new Color(255, 150, 150);
                }

                // Środek rzędy 7–9
                if (row == 7 || row == 8) {
                    if (col == 2 || col == 6 || col == 10 || col == 14) fill = Color.LIGHT_GRAY;
                    if (col >= 3 && col <= 5) fill = new Color(150, 255, 150);
                    if (col >= 11 && col <= 13) fill = new Color(150, 150, 255);
                    if (col >= 7 && col <= 9) fill = Color.WHITE; // meta środkowa
                }
                if (row == 9) {
                    if (col == 2 || col == 6 || col == 10 || col == 14) fill = Color.LIGHT_GRAY;
                    if (col >= 3 && col <= 5) fill = new Color(150, 255, 150);
                    if (col >= 11 && col <= 13) fill = new Color(150, 150, 255);
                    if (col >= 7 && col <= 9) fill = Color.WHITE;
                    if (col == 2) fill = Color.GREEN; // tu wychodzi zielony
                    if (col == 14) fill = Color.BLUE; // tu wychodzi niebieski
                }

                // Dolna część rzędy 10–14: Gx trasa, Hx żółty cel, Ix trasa
                if (row >= 10 && row <= 14) {
                    if (col == 7 || col == 9) fill = Color.LIGHT_GRAY;
                    if (col == 8) fill = new Color(255, 255, 150);
                }

                // Rząd 15: G15–H15 trasa, I15 start żółty
                if (row == 15 && (col == 7 || col == 8)) fill = Color.LIGHT_GRAY;
                if (row == 15 && col == 9) fill = Color.YELLOW;

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
                int col = t.getCol() - 1; // 0-based
                int row = t.getRow() - 1;
                int x = col * size;
                int y = row * size;

                g.setColor(convertColor(p.getColor()));
                // rysujemy circle (okrąg)
                g.fillOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
                g.setColor(Color.BLACK);
                g.drawOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
            }
        }

        // Rysujemy pionki w domach (HOME) jako circle w odpowiednich slotach
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

        int val = game.getDiceValue();
        g.setFont(new Font("Arial", Font.BOLD, Math.max(18, tileSize / 2)));
        String s = String.valueOf(val);
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

        // tło półprzezroczyste
        Color bg = new Color(255, 255, 255, 200);
        g.setColor(bg);
        g.fillRect(x - 6, y - textHeight, textWidth + 12, textHeight + 6);

        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
    }
}
