package main;

import game.Game;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException | UnsupportedLookAndFeelException |
                    IllegalAccessException | InstantiationException e)
            {
                e.printStackTrace();
            }
            var game = new Game();
            game.start();
        });
    }
}