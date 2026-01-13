package com.chinczyk;

import com.chinczyk.logic.Game;
import com.chinczyk.ui.GameFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            GameFrame frame = new GameFrame(game);
            frame.setVisible(true);
        });
    }
}
