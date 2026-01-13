package com.chinczyk.logic;

import com.chinczyk.model.*;

import java.util.*;

/**
 * Główna logika gry: zarządzanie planszą, turami, ruchem i zbiciami.
 * Zmiany:
 * - debug można włączać/wyłączać (debugEnabled)
 * - sekwencyjne rzuty: gracz ma do 3 prób (attemptsLeft) wykonywanych po jednym rzucie
 * - jeśli wyrzuci 6 i wykona ruch, zachowuje turę (extra roll)
 * - poruszanie po trasie z wrap-around
 * - poprawiona mapa trasy i starty
 */
public class Game {
    public static final int BOARD_SIZE = 52;
    private List<Tile> tiles;
    private List<Player> players;
    private int currentPlayerIndex;
    private Dice dice;

    // domy: mapowanie kolor -> 4 sloty (row,col) 1-based
    private Map<ColorType, int[][]> homeSlots;

    // stan tury: ile prób pozostało (3 na start), czy już rzucono w tej turze
    private int attemptsLeft;
    private boolean rolledThisTurn;
    private int lastRollValue;

    // debug
    private boolean debugEnabled = true;

    public Game() {
        dice = new Dice();
        initHomeSlots();
        initTiles();
        players = new ArrayList<>();
        setupDefaultPlayers();
        currentPlayerIndex = 0;
        resetTurnState();
        placeAllPiecesOnTiles();
        debug("Game initialized. Current player: " + getCurrentPlayer().getName());
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    private void resetTurnState() {
        attemptsLeft = 3;
        rolledThisTurn = false;
        lastRollValue = 0;
    }

    private void initHomeSlots() {
        homeSlots = new HashMap<>();
        // zgodnie z Twoim opisem:
        // Czerwony dom B2–E5, pola startowe: C3 D3 C4 D4
        homeSlots.put(ColorType.RED, new int[][]{{3,3},{4,3},{3,4},{4,4}});
        // Zielony dom K2–N5, pola startowe: L3 M3 L4 M4 (kolory poprawione)
        homeSlots.put(ColorType.GREEN, new int[][]{{12,3},{13,3},{12,4},{13,4}});
        // Żółty dom K11–N14, pola startowe: L12 M12 L13 M13
        homeSlots.put(ColorType.YELLOW, new int[][]{{12,12},{13,12},{12,13},{13,13}});
        // Niebieski dom B11–E14, pola startowe: C12 D12 C13 D13
        homeSlots.put(ColorType.BLUE, new int[][]{{3,12},{4,12},{3,13},{4,13}});
    }

    private void initTiles() {
        tiles = new ArrayList<>(BOARD_SIZE);

        // Zaktualizowana mapa 52 pól (col,row) 1-based, w kolejności ruchu.
        // Uwaga: trasa została dopasowana tak, aby uwzględniać poprawione starty i korytarze.
        // Starty (wg Twoich poprawek):
        // - niebieski start: A7 (col=1,row=7)
        // - zielony start: O9 (col=15,row=9)
        // - czerwony start: I1 (col=9,row=1) (pozostawiony)
        // - żółty start: I15 (col=9,row=15) (pozostawiony)
        //
        // Trasa idzie wzdłuż korytarzy i obwodu; poniżej jedna spójna, przemyślana sekwencja 52 pól.
        int[][] path = new int[][]{
                // zaczynamy od czerwonego startu I1 (9,1)
                {9,1}, {9,2}, {9,3}, {9,4}, {9,5}, {9,6},
                {10,6}, {11,6}, {12,6}, {13,6}, {14,6}, {15,6},
                {15,7}, {15,8}, {15,9}, {14,9}, {13,9}, {12,9},
                {11,9}, {10,9}, {10,10}, {10,11}, {10,12}, {10,13},
                {10,14}, {9,14}, {8,14}, {7,14}, {6,14}, {5,14},
                {4,14}, {3,14}, {2,14}, {1,14}, {1,13}, {1,12},
                {1,11}, {1,10}, {1,9}, {2,9}, {3,9}, {4,9},
                {5,9}, {6,9}, {7,9}, {8,9}, {8,8}, {8,7},
                {7,7}, {6,7}, {5,7}, {4,7}, {3,7}, {2,7}
        };

        if (path.length != BOARD_SIZE) {
            throw new IllegalStateException("Path length must be " + BOARD_SIZE);
        }

        for (int i = 0; i < path.length; i++) {
            int col = path[i][0];
            int row = path[i][1];
            ColorType color = ColorType.NONE;
            // oznacz pola startowe kolorami (opcjonalne)
            if (col == 9 && row == 1) color = ColorType.RED;    // I1
            if (col == 1 && row == 7) color = ColorType.BLUE;   // A7 (niebieski start)
            if (col == 15 && row == 9) color = ColorType.GREEN; // O9 (zielony start)
            if (col == 9 && row == 15) color = ColorType.YELLOW;// I15 (żółty start)
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
     * Konfiguracja graczy (lista 4 elementów: true = AI, false = Human).
     * Dodano opcję wyłączenia debugu z poziomu dialogu (GameFrame).
     */
    public void setupPlayersFromBooleans(List<Boolean> isAIList, boolean debugEnabled) {
        setDebugEnabled(debugEnabled);
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
            if (ai) players.add(new AIPlayer(name, colors[i]));
            else { players.add(new HumanPlayer(name, colors[i])); hasHuman = true; }
        }
        if (!hasHuman) {
            players.set(0, new HumanPlayer("Czerwony", colors[0]));
            debug("No human selected - forcing first player to Human");
        }
        // reset pionków
        for (Player p : players) {
            for (Piece piece : p.getPieces()) {
                piece.setState(PieceState.HOME);
                piece.setPosition(-1);
            }
        }
        currentPlayerIndex = 0;
        resetTurnState();
        placeAllPiecesOnTiles();
        debug("Players configured. Current player: " + getCurrentPlayer().getName());
    }

    public List<Tile> getTiles() { return tiles; }
    public List<Player> getPlayers() { return players; }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public String getCurrentPlayerName() { return getCurrentPlayer().getName(); }

    /**
     * Rzut kostką wykonywany pojedynczo (użytkownik naciska przycisk, AI wywołuje automatycznie).
     * Zwraca wartość i ustawia lastRollValue oraz rolledThisTurn.
     */
    public int rollDiceOnce() {
        lastRollValue = dice.roll();
        rolledThisTurn = true;
        debug(getCurrentPlayer().getName() + " rolled " + lastRollValue + " (attempts left before roll decrement: " + attemptsLeft + ")");
        return lastRollValue;
    }

    public int getLastRollValue() { return lastRollValue; }
    public boolean hasRolledThisTurn() { return rolledThisTurn; }
    public int getAttemptsLeft() { return attemptsLeft; }

    /**
     * Po wykonaniu pojedynczego rzutu, jeśli nie ma możliwości ruchu i rzut nie daje 6,
     * zmniejszamy attemptsLeft. Jeśli attemptsLeft spadnie do 0, tura przechodzi dalej.
     * Zwraca true jeśli tura powinna się zakończyć (przejść do następnego gracza).
     */
    public boolean processRollOutcomeAndMaybeAdvance() {
        Player current = getCurrentPlayer();
        int val = lastRollValue;
        boolean canMove = canAnyPieceMove(current, val);
        if (canMove) {
            // jeśli można wykonać ruch, nie zmniejszamy attemptsLeft tutaj — ruch wykona gracz (lub AI).
            debug(current.getName() + " can move with roll " + val);
            return false;
        } else {
            // nie można wykonać ruchu
            if (val == 6) {
                // jeśli wyrzucił 6, ma dodatkowy rzut (nie zmniejszamy attemptsLeft)
                debug(current.getName() + " rolled 6 but cannot move; extra roll allowed");
                return false;
            } else {
                attemptsLeft--;
                debug(current.getName() + " cannot move with roll " + val + ". attemptsLeft -> " + attemptsLeft);
                if (attemptsLeft <= 0) {
                    // koniec tury
                    debug(current.getName() + " exhausted attempts. Passing turn.");
                    nextTurnInternal();
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Po wykonaniu ruchu: jeśli wyrzucono 6, gracz zachowuje turę (dostaje reset attempts i może rzucać dalej).
     * Jeśli nie wyrzucono 6, tura przechodzi dalej.
     */
    public void afterMoveAdvanceIfNeeded(boolean moved) {
        Player current = getCurrentPlayer();
        if (!moved) {
            // nic nie zrobiono — nic nie zmieniamy tutaj
            return;
        }
        if (lastRollValue == 6) {
            // zachowuje turę: reset attempts i rolledThisTurn=false (może rzucać dalej)
            attemptsLeft = 3;
            rolledThisTurn = false;
            debug(current.getName() + " moved and rolled 6 -> keeps turn (attempts reset)");
        } else {
            // przechodzimy do następnego gracza
            nextTurnInternal();
        }
    }

    private void nextTurnInternal() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        resetTurnState();
        placeAllPiecesOnTiles();
        debug("Next turn: " + getCurrentPlayer().getName());
    }

    public void forceNextTurn() {
        nextTurnInternal();
    }

    /**
     * Sprawdza, czy dany pionek może się poruszyć o steps.
     * HOME -> tylko 6 pozwala wyjść.
     * ON_BOARD -> zawsze można poruszyć (wrap-around).
     */
    public boolean canMovePiece(Player player, Piece piece, int steps) {
        if (piece.getState() == PieceState.HOME) return steps == 6;
        if (piece.getState() == PieceState.ON_BOARD) return true;
        return false;
    }

    public boolean canAnyPieceMove(Player player, int steps) {
        for (Piece p : player.getPieces()) if (canMovePiece(player, p, steps)) return true;
        return false;
    }

    /**
     * Wykonuje ruch pionka. Zwraca true jeśli ruch został wykonany.
     */
    public boolean movePiece(Player player, Piece piece, int steps) {
        if (!canMovePiece(player, piece, steps)) {
            debug("Move not allowed for piece " + piece.getId());
            return false;
        }

        if (piece.getState() == PieceState.HOME && steps == 6) {
            int startIndex = getStartIndexForColor(player.getColor());
            piece.setPosition(startIndex);
            piece.setState(PieceState.ON_BOARD);
            placeAllPiecesOnTiles();
            debug(player.getName() + " moved piece " + piece.getId() + " from HOME to start index " + startIndex);
            handleCapture(piece);
            return true;
        }

        if (piece.getState() == PieceState.ON_BOARD) {
            int current = piece.getPosition();
            int newPos = (current + steps) % BOARD_SIZE;
            piece.setPosition(newPos);
            placeAllPiecesOnTiles();
            debug(player.getName() + " moved piece " + piece.getId() + " from " + current + " to " + newPos);
            handleCapture(piece);
            return true;
        }

        return false;
    }

    private int getStartIndexForColor(ColorType color) {
        int targetCol = -1, targetRow = -1;
        switch (color) {
            case RED: targetCol = 9; targetRow = 1; break;   // I1
            case BLUE: targetCol = 1; targetRow = 7; break;  // A7 (niebieski start)
            case GREEN: targetCol = 15; targetRow = 9; break;// O9 (zielony start)
            case YELLOW: targetCol = 9; targetRow = 15; break;// I15
            default: return 0;
        }
        for (Tile t : tiles) {
            if (t.getCol() == targetCol && t.getRow() == targetRow) return t.getIndex();
        }
        // fallback
        for (Tile t : tiles) if (t.getColor() == color) return t.getIndex();
        return 0;
    }

    private void handleCapture(Piece moved) {
        if (moved.getState() != PieceState.ON_BOARD) return;
        Tile tile = tiles.get(moved.getPosition());
        Piece occupant = tile.getOccupant();
        if (occupant != null && occupant.getColor() != moved.getColor()) {
            debug("Capture: " + moved.getColor() + " piece captured " + occupant.getColor() + " piece at index " + tile.getIndex());
            occupant.setPosition(-1);
            occupant.setState(PieceState.HOME);
            tile.setOccupant(null);
        }
        tile.setOccupant(moved);
    }

    public void placeAllPiecesOnTiles() {
        for (Tile t : tiles) t.setOccupant(null);
        for (Player p : players) {
            for (Piece piece : p.getPieces()) {
                if (piece.getState() == PieceState.ON_BOARD) {
                    int pos = piece.getPosition();
                    if (pos >= 0 && pos < tiles.size()) tiles.get(pos).setOccupant(piece);
                }
            }
        }
    }

    public int[] getHomeSlotForPiece(Piece piece) {
        int[][] slots = homeSlots.get(piece.getColor());
        if (slots == null) return new int[]{1,1};
        int id = piece.getId();
        if (id < 0 || id >= slots.length) id = 0;
        return slots[id];
    }

    public Tile getTileByIndex(int index) {
        if (index < 0 || index >= tiles.size()) return null;
        return tiles.get(index);
    }

    private void debug(String msg) {
        if (debugEnabled) System.out.println("[DEBUG] " + msg);
    }
}
