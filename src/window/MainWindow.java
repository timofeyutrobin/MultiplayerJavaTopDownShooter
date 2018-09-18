package window;

import game.Game;

import javax.swing.*;

public class MainWindow extends JFrame {

    public MainWindow(Game game, String title) {
        this.setTitle(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.getContentPane().add(game);

        this.pack();
        //this.setVisible(true);
    }
}
