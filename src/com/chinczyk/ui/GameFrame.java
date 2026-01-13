package com.chinczyk.ui;

import com.chinczyk.logic.Game;
import com.chinczyk.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Główne okno gry. Zawiera panel planszy, menu konfiguracji graczy i przyciski sterujące.
 * Zmieniono:
 * - usunięto prawy boczny label "Tura: ..." (zostawiony tylko dolny wskaźnik na planszy),
 * - implementacja 3 rzutów przy starcie tury (dla AI i Human),
 * - AI wykonuje automatycznie swoje tury (rzut + ruch).
 */
public class GameFrame extends JFrame {
    private Game game;
    private BoardPanel boardPanel;
    private JButton rollButton;
    private JLabel infoLabel;

    public GameFrame(Game game) {
        super("Chińczyk");
        this.game = game;
        initUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI() {
        boardPanel = new BoardPanel(game);
        boardPanel.setPreferredSize(new Dimension(720, 720));

        rollButton = new JButton("Rzuć kostką");
        rollButton.addActionListener(e -> handleRollButton());

        infoLabel = new JLabel("Kliknij Rzuć kostką aby rozpocząć");

        JPanel control = new JPanel();
        control.setLayout(new BorderLayout());
        control.add(rollButton, BorderLayout.CENTER);
        control.add(infoLabel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(control, BorderLayout.SOUTH);

        createMenuBar();

        // jeśli pierwszy gracz jest AI, uruchamiamy automatycznie AI tury
        SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGameItem = new JMenuItem(new AbstractAction("New Game...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewGameDialog();
            }
        });

        JMenuItem exitItem = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void openNewGameDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        String[] options = new String[]{"Human", "AI"};
        JComboBox<String> p1 = new JComboBox<>(options);
        JComboBox<String> p2 = new JComboBox<>(options);
        JComboBox<String> p3 = new JComboBox<>(options);
        JComboBox<String> p4 = new JComboBox<>(options);

        // domyślnie: pierwszy Human, reszta AI
        p1.setSelectedIndex(0);
        p2.setSelectedIndex(1);
        p3.setSelectedIndex(1);
        p4.setSelectedIndex(1);

        panel.add(new JLabel("Player 1 (Czerwony):"));
        panel.add(p1);
        panel.add(new JLabel("Player 2 (Zielony):"));
        panel.add(p2);
        panel.add(new JLabel("Player 3 (Niebieski):"));
        panel.add(p3);
        panel.add(new JLabel("Player 4 (Żółty):"));
        panel.add(p4);

        int result = JOptionPane.showConfirmDialog(this, panel, "New Game - wybierz Human/AI", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<Boolean> isAI = new ArrayList<>();
            isAI.add(p1.getSelectedIndex() == 1);
            isAI.add(p2.getSelectedIndex() == 1);
            isAI.add(p3.getSelectedIndex() == 1);
            isAI.add(p4.getSelectedIndex() == 1);

            boolean anyHuman = isAI.stream().anyMatch(b -> !b);
            if (!anyHuman) {
                int choice = JOptionPane.showConfirmDialog(this, "Nie wybrano żadnego gracza ludzkiego. Chcesz wymusić Player 1 jako Human?", "Brak gracza ludzkiego", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    isAI.set(0, false);
                } else {
                    isAI.set(0, false);
                }
            }

            game.setupPlayersFromBooleans(isAI);
            game.placeAllPiecesOnTiles();
            boardPanel.repaint();
            infoLabel.setText("Nowa gra. Tura: " + game.getCurrentPlayerName());
            SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
        }
    }

    /**
     * Obsługa przycisku Rzuć kostką:
     * - wykonujemy do 3 rzutów jeśli nie ma możliwości ruchu (np. wszystkie pionki w domu i nie wyrzucono 6),
     * - jeśli gracz jest AI i ma możliwość ruchu, AI wykona ruch automatycznie,
     * - jeśli gracz jest Human i ma możliwość ruchu, czekamy na kliknięcie planszy (BoardPanel obsługuje ruch).
     */
    private void handleRollButton() {
        Player current = game.getCurrentPlayer();

        // wykonujemy do 3 rzutów, zatrzymujemy się gdy pojawi się ruch możliwy do wykonania
        int maxRolls = 3;
        boolean canMove = false;
        int lastRoll = 0;
        for (int attempt = 1; attempt <= maxRolls; attempt++) {
            lastRoll = game.rollDice();
            infoLabel.setText(current.getName() + " wyrzucił: " + lastRoll + " (próba " + attempt + "/" + maxRolls + ")");
            boardPanel.repaint();

            if (game.canAnyPieceMove(current, lastRoll)) {
                canMove = true;
                break;
            } else {
                // jeśli nie może poruszyć, kontynuujemy kolejne próby
                try {
                    Thread.sleep(150); // krótka pauza by UI odświeżyło kostkę
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!canMove) {
            // po 3 nieudanych rzutach tura przechodzi dalej
            infoLabel.setText(current.getName() + " nie może wykonać ruchu po " + maxRolls + " rzutach. Przechodzimy dalej.");
            game.nextTurn();
            boardPanel.repaint();
            // jeśli następny jest AI, uruchamiamy AI tury
            SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
            return;
        }

        // jeśli można wykonać ruch:
        if (!(current instanceof com.chinczyk.model.HumanPlayer)) {
            // AI: wykonaj ruch automatycznie (wybierz pierwszy możliwy pionek)
            current.takeTurn(game, game.getDiceValue());
            game.placeAllPiecesOnTiles();
            boardPanel.repaint();
            game.nextTurn();
            infoLabel.setText(current.getName() + " (AI) wykonał ruch.");
            // po ruchu AI sprawdź kolejne AI
            SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
        } else {
            // Human: czekamy na kliknięcie planszy, które wykona ruch (BoardPanel.handleClick)
            infoLabel.setText(current.getName() + " może wykonać ruch. Kliknij pionek lub pole.");
        }
    }

    /**
     * Jeśli aktualny gracz jest AI, wykonaj automatycznie jego turę (rzut + ruch).
     * Pętla wykonuje kolejne tury AI aż do momentu gdy natrafi na gracza ludzkiego.
     */
    private void handleAITurnsIfNeeded() {
        // wykonujemy AI tury w pętli, ale z krótkimi przerwami, aby UI mogło się odświeżyć
        while (!(game.getCurrentPlayer() instanceof com.chinczyk.model.HumanPlayer)) {
            Player ai = game.getCurrentPlayer();
            // AI ma do 3 prób, analogicznie do gracza
            int maxRolls = 3;
            boolean canMove = false;
            int lastRoll = 0;
            for (int attempt = 1; attempt <= maxRolls; attempt++) {
                lastRoll = game.rollDice();
                infoLabel.setText(ai.getName() + " (AI) wyrzucił: " + lastRoll + " (próba " + attempt + "/" + maxRolls + ")");
                boardPanel.repaint();
                if (game.canAnyPieceMove(ai, lastRoll)) {
                    canMove = true;
                    break;
                } else {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (!canMove) {
                infoLabel.setText(ai.getName() + " (AI) nie może wykonać ruchu po " + maxRolls + " rzutach.");
                game.nextTurn();
                boardPanel.repaint();
                continue; // sprawdź następnego gracza (może też być AI)
            }

            // AI wykonuje ruch (AIPlayer.takeTurn wybiera pierwszy możliwy pionek)
            ai.takeTurn(game, game.getDiceValue());
            game.placeAllPiecesOnTiles();
            boardPanel.repaint();
            game.nextTurn();

            try {
                Thread.sleep(250); // krótka pauza między AI ruchami
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
