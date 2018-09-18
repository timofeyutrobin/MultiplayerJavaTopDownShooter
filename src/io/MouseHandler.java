package io;

import game.Game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    private Game game;

    public MouseHandler(Game game) {
        this.game = game;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        game.mousePressed();
    }
}
