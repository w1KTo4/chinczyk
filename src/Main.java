import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame okno = new JFrame("Chińczyk");
        okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        okno.setResizable(false);
        okno.add(new Panel()); // Panel korzysta z nowej, rozbitej architektury
        okno.pack();
        okno.setLocationRelativeTo(null);
        okno.setVisible(true);
    }
}
