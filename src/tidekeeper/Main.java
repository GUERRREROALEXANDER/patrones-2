package tidekeeper;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import tidekeeper.ui.Dashboard;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            JFrame window = new JFrame("Tidekeeper | Maritime Heritage Conservation");
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            window.setContentPane(new Dashboard());
            window.setMinimumSize(new Dimension(1000, 760));
            window.setSize(1200, 850);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
