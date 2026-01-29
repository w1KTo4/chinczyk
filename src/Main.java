import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame okno = new JFrame("Chińczyk");
            okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            okno.setResizable(false);

            Panel panel = new Panel();

            JPanel topControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            topControls.setOpaque(false); 

            JButton btnRestart = new JButton("Restart");
            JButton btnExit = new JButton("Wyjście");

            btnRestart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panel.restartGame();
                }
            });

            btnExit.addActionListener(e -> System.exit(0));

            topControls.add(btnRestart);
            topControls.add(btnExit);

            okno.setLayout(new BorderLayout());
            okno.add(topControls, BorderLayout.NORTH);
            okno.add(panel, BorderLayout.CENTER);

            okno.pack();
            okno.setLocationRelativeTo(null);
            okno.setVisible(true);
        });
    }
}
