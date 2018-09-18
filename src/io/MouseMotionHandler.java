package io;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MouseMotionHandler extends MouseMotionAdapter {
    private int x, y;

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
