package br.com.vacaestelar;

import javax.swing.*;
import java.awt.*;
import br.com.vacaestelar.ui.UniversePanel;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Vaca Estelar | Danca das constelacoes");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new UniversePanel());
            window.pack();
            window.setMinimumSize(new Dimension(800, 600));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
