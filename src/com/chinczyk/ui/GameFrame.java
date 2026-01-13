package com.chinczyk.ui;

import com.chinczyk.logic.Game;
import com.chinczyk.model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Okno gry: menu konfiguracji (Human/AI + debug on/off), przycisk Rzuć kostką,
 * sekwencyjne rzuty (po jednym), AI automatycznie wykonuje swoje ruchy.
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
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        String[] options = new String[]{"Human", "AI"};
        JComboBox<String> p1 = new JComboBox<>(options);
        JComboBox<String> p2 = new JComboBox<>(options);
        JComboBox<String> p3 = new JComboBox<>(options);
        JComboBox<String> p4 = new JComboBox<>(options);
        JCheckBox debugBox = new JCheckBox("Włącz debug (konsola)", true);

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
        panel.add(new JLabel("Debug:"));
        panel.add(debugBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "New Game - wybierz Human/AI i debug", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            List<Boolean> isAI = new ArrayList<>();
            isAI.add(p1.getSelectedIndex() == 1);
            isAI.add(p2.getSelectedIndex() == 1);
            isAI.add(p3.getSelectedIndex() == 1);
            isAI.add(p4.getSelectedIndex() == 1);

            boolean anyHuman = isAI.stream().anyMatch(b -> !b);
            if (!anyHuman) {
                int choice = JOptionPane.showConfirmDialog(this, "Nie wybrano żadnego gracza ludzkiego. Wymusić Player 1 jako Human?", "Brak gracza ludzkiego", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) isAI.set(0, false);
                else isAI.set(0, false);
            }

            game.setupPlayersFromBooleans(isAI, debugBox.isSelected());
            game.placeAllPiecesOnTiles();
            boardPanel.repaint();
            infoLabel.setText("Nowa gra. Tura: " + game.getCurrentPlayerName());
            SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
        }
    }

    /**
     * Obsługa przycisku Rzuć kostką: wykonuje pojedynczy rzut.
     * - jeśli po rzucie można wykonać ruch, dla AI ruch wykonuje się automatycznie,
     * - jeśli nie można i rzut != 6, attemptsLeft-- i gracz musi ponownie nacisnąć (do 3 razy),
     * - jeśli attemptsLeft==0 -> tura przechodzi dalej.
     */
    private void handleRollButton() {
        Player current = game.getCurrentPlayer();

        // jeśli już rzucono i nie wykonano ruchu, nadal pozwalamy na kolejny rzut (sekwencyjnie)
        int val = game.rollDiceOnce();
        boardPanel.repaint();
        infoLabel.setText(current.getName() + " wyrzucił: " + val);

        // sprawdź czy można wykonać ruch
        boolean canMove = game.canAnyPieceMove(current, val);
        if (!canMove) {
            // jeśli nie można, przetwarzamy wynik (zmniejszamy attempts lub dajemy extra roll jeśli 6)
            boolean turnEnded = game.processRollOutcomeAndMaybeAdvance();
            boardPanel.repaint();
            if (turnEnded) {
                infoLabel.setText(current.getName() + " nie może wykonać ruchu po 3 rzutach. Przechodzimy dalej.");
                // jeśli następny jest AI, uruchom AI
                SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
            } else {
                if (val == 6) infoLabel.setText(current.getName() + " wyrzucił 6 — ma dodatkowy rzut.");
                else infoLabel.setText(current.getName() + " nie może wykonać ruchu. Pozostało prób: " + game.getAttemptsLeft());
            }
            return;
        }

        // jeśli można wykonać ruch:
        if (!(current instanceof com.chinczyk.model.HumanPlayer)) {
            // AI: wykonaj ruch natychmiast (AI wybiera najlepszy ruch)
            current.takeTurn(game, val);
            game.placeAllPiecesOnTiles();
            boardPanel.repaint();
            // po ruchu sprawdź czy zachowuje turę (6) czy przechodzi dalej
            game.afterMoveAdvanceIfNeeded(true);
            infoLabel.setText(current.getName() + " (AI) wykonał ruch.");
            // jeśli następny jest AI, kontynuujemy pętlę AI
            SwingUtilities.invokeLater(this::handleAITurnsIfNeeded);
        } else {
            // Human: czekamy na kliknięcie planszy, które wykona ruch (BoardPanel.handleClick)
            infoLabel.setText(current.getName() + " może wykonać ruch. Kliknij pionek lub pole startowe.");
        }
    }

    /**
     * Pętla AI: wykonuje pojedyncze rzuty i ruchy AI aż do momentu gdy natrafi na gracza ludzkiego.
     * Każdy rzut jest pojedynczy (AI ma do 3 prób, analogicznie do gracza).
     */
    private void handleAITurnsIfNeeded() {
        while (!(game.getCurrentPlayer() instanceof com.chinczyk.model.HumanPlayer)) {
            Player ai = game.getCurrentPlayer();
            // AI wykonuje pojedyncze rzuty sekwencyjnie (do 3 prób)
            boolean moved = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                int val = game.rollDiceOnce();
                infoLabel.setText(ai.getName() + " (AI) wyrzucił: " + val + " (próba " + attempt + "/3)");
                boardPanel.repaint();
                if (game.canAnyPieceMove(ai, val)) {
                    ai.takeTurn(game, val);
                    game.placeAllPiecesOnTiles();
                    boardPanel.repaint();
                    moved = true;
                    game.afterMoveAdvanceIfNeeded(true);
                    break;
                } else {
                    boolean ended = game.processRollOutcomeAndMaybeAdvance();
                    boardPanel.repaint();
                    if (ended) break;
                }
                try { Thread.sleep(200); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
            if (!moved) {
                // jeśli AI nie wykonał ruchu po 3 próbach, nextTurn już wykonane w processRollOutcomeAndMaybeAdvance
                continue;
            }
            // jeśli AI wykonał ruch i zachował turę (6), pętla będzie kontynuowana; w przeciwnym razie nextTurn już wykonane
            try { Thread.sleep(250); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }
}
