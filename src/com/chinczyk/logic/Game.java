package com.chinczyk.logic;

import com.chinczyk.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Główna logika gry: zarządzanie planszą, turami, ruchem i zbiciami.
 * Zawiera mapę trasy 52 pól z dokładnymi współrzędnymi na planszy 15x15.
 * Dodano debugowanie (System.out) informujące o ruchach, rzutach i zbiciach.
 * Przepisano poruszanie: poruszamy się po liście tiles (0..51) z wrap-around.
 */
public class Game {
    public static final int BOARD_SIZE = 52; // długość trasy
    private List<Tile> tiles;                // 52 pola trasy
    private List<Player> players;
    private int currentPlayerIndex;
    private Dice dice;

    // mapowanie domów (kolor -> lista współrzędnych 2x2 startowych pól domowych)
    private Map<ColorType, int[][]> homeSlots;

    public Game() {
        dice = new Dice();
        initHomeSlots();
        initTiles();
        players = new ArrayList<>();
        setupDefaultPlayers();
        currentPlayerIndex = 0;
        placeAllPiecesOnTiles();
        debug("Game initialized. Current player: " + getCurrentPlayer().getName());
    }

    private void initHomeSlots() {
        homeSlots = new HashMap<>();
        // Każdy wpis: 4 pól startowych (row,col) 1-based
        // Czerwony dom B2–E5, pola startowe: C3 D3 C4 D4
        homeSlots.put(ColorType.RED, new int[][]{
                {3,3}, {4,3}, {3,4}, {4,4}
        });
        // Zielony dom K2–N5, pola startowe: L3 M3 L4 M4
        homeSlots.put(ColorType.GREEN, new int[][]{
                {12,3}, {13,3}, {12,4}, {13,4}
        });
        // Żółty dom K11–N14, pola startowe: L12 M12 L13 M13
        homeSlots.put(ColorType.YELLOW, new int[][]{
                {12,12}, {13,12}, {12,13}, {13,13}
        });
        // Niebieski dom B11–E14, pola startowe: C12 D12 C13 D13
        homeSlots.put(ColorType.BLUE, new int[][]{
                {3,12}, {4,12}, {3,13}, {4,13}
        });
    }

    private void initTiles() {
        tiles = new ArrayList<>(BOARD_SIZE);

        // Mapa 52 pól (col,row) 1-based, w kolejności ruchu.
        // Dopasowana do układu 15x15 i punktów startowych:
        // start RED: I1 (9,1)
        // start GREEN: B9 (2,9)
        // start BLUE: N9 (14,9)
        // start YELLOW: I15 (9,15) - jeśli nie ma w path, fallback
        int[][] path = new int[][]{
                // 0..51
                {9,1}, {9,2}, {9,3}, {9,4}, {9,5}, {9,6},         // I1..I6
                {10,6}, {11,6}, {12,6}, {13,6}, {14,6}, {15,6},    // J6..O6
                {15,7}, {15,8}, {15,9},                            // O7..O9
                {14,9}, {13,9}, {12,9}, {11,9}, {10,9},            // N9..J9
                {10,10}, {10,11}, {10,12}, {10,13}, {10,14},       // J10..J14
                {9,14}, {8,14}, {7,14}, {6,14}, {5,14}, {4,14},   // I14..D14
                {3,14}, {2,14}, {2,13}, {2,12}, {2,11},            // C14..B11
                {3,11}, {4,11}, {5,11}, {6,11}, {7,11}, {8,11},    // C11..H11
                {8,10}, {8,9}, {8,8}, {8,7},                       // H10..H7
                {7,7}, {6,7}, {5,7}, {4,7}, {3,7}, {2,7}           // G7..B7
        };

        if (path.length != BOARD_SIZE) {
            throw new IllegalStateException("Path length must be " + BOARD_SIZE);
        }

        for (int i = 0; i < path.length; i++) {
            int col = path[i][0];
            int row = path[i][1];
            ColorType color = ColorType.NONE;
            // oznaczamy pola startowe kolorem (opcjonalne)
            if (col == 9 && row == 1) color = ColorType.RED;
            if (col == 2 && row == 9) color = ColorType.GREEN;
            if (col == 14 && row == 9) color = ColorType.BLUE;
            if (col == 9 && row == 15) color = ColorType.YELLOW;
            tiles.add(new Tile(i, row, col, color));
        }
    }

    private void setupDefaultPlayers() {
        players.clear();
        players.add(new HumanPlayer("Czerwony", ColorType.RED));
        players.add(new HumanPlayer("Zielony", ColorType.GREEN));
        players.add(new HumanPlayer("Niebieski", ColorType.BLUE));
        players.add(new HumanPlayer("Żółty", ColorType.YELLOW));
    }

    /**
     * Konfiguruje graczy na podstawie listy typów.
     * Lista powinna mieć długość 4; element true = AI, false = Human.
     * Zapewnia co najmniej jednego gracza ludzkiego (jeśli brak, pierwszy zostaje ustawiony na Human).
     */
    public void setupPlayersFromBooleans(List<Boolean> isAIList) {
        if (isAIList == null || isAIList.size() != 4) {
            throw new IllegalArgumentException("isAIList must contain 4 elements");
        }
        players.clear();
        ColorType[] colors = new ColorType[]{ColorType.RED, ColorType.GREEN, ColorType.BLUE, ColorType.YELLOW};
        boolean hasHuman = false;
        for (int i = 0; i < 4; i++) {
            boolean ai = isAIList.get(i);
            String name = switch (i) {
                case 0 -> "Czerwony";
                case 1 -> "Zielony";
                case 2 -> "Niebieski";
                default -> "Żółty";
            };
            if (ai) {
                players.add(new AIPlayer(name, colors[i]));
            } else {
                players.add(new HumanPlayer(name, colors[i]));
                hasHuman = true;
            }
        }
        if (!hasHuman) {
            // wymuszamy przynajmniej jednego człowieka - ustawiamy pierwszego
            players.set(0, new HumanPlayer("Czerwony", colors[0]));
            debug("No human selected - forcing first player to Human");
        }
        // resetujemy pionki do HOME
        for (Player p : players) {
            for (Piece piece : p.getPieces()) {
                piece.setState(PieceState.HOME);
                piece.setPosition(-1);
            }
        }
        currentPlayerIndex = 0;
        placeAllPiecesOnTiles();
        debug("Players configured. Current player: " + getCurrentPlayer().getName());
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public String getCurrentPlayerName() {
        return getCurrentPlayer().getName();
    }

    /**
     * Rzuca kostką i zwraca wartość. Aktualizuje wewnętrzne lastValue.
     */
    public int rollDice() {
        int val = dice.roll();
        debug(getCurrentPlayer().getName() + " rolled " + val);
        return val;
    }

    public int getDiceValue() {
        return dice.getLastValue();
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        debug("Next turn: " + getCurrentPlayer().getName());
    }

    /**
     * Sprawdza, czy dany pionek może się poruszyć o steps (prosta reguła).
     * HOME -> tylko 6 pozwala wyjść.
     * ON_BOARD -> zawsze można poruszyć (wrap-around).
     */
    public boolean canMovePiece(Player player, Piece piece, int steps) {
        if (piece.getState() == PieceState.HOME) {
            return steps == 6;
        }
        if (piece.getState() == PieceState.ON_BOARD) {
            // prosta reguła: zawsze można poruszyć po trasie (wrap-around)
            return true;
        }
        return false;
    }

    /**
     * Sprawdza, czy którykolwiek pionek gracza może wykonać ruch przy danej wartości kostki.
     */
    public boolean canAnyPieceMove(Player player, int steps) {
        for (Piece p : player.getPieces()) {
            if (canMovePiece(player, p, steps)) return true;
        }
        return false;
    }

    /**
     * Wykonuje ruch pionka: jeśli HOME i steps==6 -> ustawia na startIndex.
     * Jeśli ON_BOARD -> przesuwa po trasie z wrap-around.
     * Obsługuje zbicia.
     */
    public void movePiece(Player player, Piece piece, int steps) {
        debug(player.getName() + " attempts to move piece " + piece.getId() + " with steps " + steps);
        if (!canMovePiece(player, piece, steps)) {
            debug("Move not allowed for piece " + piece.getId());
            return;
        }

        if (piece.getState() == PieceState.HOME && steps == 6) {
            int startIndex = getStartIndexForColor(player.getColor());
            piece.setPosition(startIndex);
            piece.setState(PieceState.ON_BOARD);
            placeAllPiecesOnTiles();
            debug(player.getName() + " moved piece " + piece.getId() + " from HOME to start index " + startIndex);
            handleCapture(piece);
            return;
        }

        if (piece.getState() == PieceState.ON_BOARD) {
            int current = piece.getPosition();
            int newPos = (current + steps) % BOARD_SIZE; // wrap-around
            piece.setPosition(newPos);
            placeAllPiecesOnTiles();
            debug(player.getName() + " moved piece " + piece.getId() + " from " + current + " to " + newPos);
            handleCapture(piece);
        }
    }

    private int getStartIndexForColor(ColorType color) {
        int targetCol = -1, targetRow = -1;
        switch (color) {
            case RED: targetCol = 9; targetRow = 1; break;   // I1
            case GREEN: targetCol = 2; targetRow = 9; break; // B9
            case BLUE: targetCol = 14; targetRow = 9; break; // N9
            case YELLOW: targetCol = 9; targetRow = 15; break; // I15 (fallback)
            default: return 0;
        }
        for (Tile t : tiles) {
            if (t.getCol() == targetCol && t.getRow() == targetRow) return t.getIndex();
        }
        // fallback: jeśli nie znaleziono, zwróć indeks pola oznaczonego tym kolorem
        for (Tile t : tiles) {
            if (t.getColor() == color) return t.getIndex();
        }
        return 0;
    }

    private void handleCapture(Piece moved) {
        if (moved.getState() != PieceState.ON_BOARD) return;
        Tile tile = tiles.get(moved.getPosition());
        Piece occupant = tile.getOccupant();
        if (occupant != null && occupant.getColor() != moved.getColor()) {
            // zbicie - wysyłamy pionek do domu
            debug("Capture: " + moved.getColor() + " piece captured " + occupant.getColor() + " piece at index " + tile.getIndex());
            occupant.setPosition(-1);
            occupant.setState(PieceState.HOME);
            tile.setOccupant(null);
        }
        tile.setOccupant(moved);
    }

    public void placeAllPiecesOnTiles() {
        // czyścimy pola trasy
        for (Tile t : tiles) t.setOccupant(null);

        // umieszczamy pionki graczy: tylko te ON_BOARD trafiają na tiles
        for (Player p : players) {
            for (Piece piece : p.getPieces()) {
                if (piece.getState() == PieceState.ON_BOARD) {
                    int pos = piece.getPosition();
                    if (pos >= 0 && pos < tiles.size()) {
                        tiles.get(pos).setOccupant(piece);
                    }
                }
            }
        }
    }

    /**
     * Zwraca współrzędne (row,col) pola domowego dla danego pionka,
     * na podstawie jego koloru i id (0..3). Używane do rysowania pionków w domach.
     */
    public int[] getHomeSlotForPiece(Piece piece) {
        int[][] slots = homeSlots.get(piece.getColor());
        if (slots == null) return new int[]{1,1};
        int id = piece.getId();
        if (id < 0 || id >= slots.length) id = 0;
        return slots[id];
    }

    private void debug(String msg) {
        System.out.println("[DEBUG] " + msg);
    }
}
