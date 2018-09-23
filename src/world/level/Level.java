package world.level;

import game.Game;
import world.objects.GameObject;
import world.objects.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private static final GradientPaint BG_GRADIENT = new GradientPaint(0, 0, Color.BLACK,
            Game.WIDTH, Game.HEIGHT, Color.LIGHT_GRAY);

    private final TileMap tileMap;
    private final ArrayList<GameObject> objects;

    private final Player player;

    //камера
    private int xOffset;
    private int yOffset;

    public Level(TileMap tileMap) {
        this.tileMap = tileMap;
        objects = new ArrayList<>();

        var username = JOptionPane.showInputDialog("Enter your username");
        if (username == null) {
            System.exit(0);
        }

        var player = new Player(this, 100, 200, username, Game.client.getIpAddress(), Game.client.getPort(), true);
        this.player = player;
        addObject(player);

        //игрок находится в центре экрана
        xOffset = player.getX() - Game.SCREEN_CENTER_X;
        yOffset = player.getY() - Game.SCREEN_CENTER_Y;
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public synchronized void addObject(GameObject object) {
        objects.add(object);
    }

    public void removePlayer(String username) {
        getPlayer(username).delete();
    }

    public Player getPlayer() {
        return player;
    }

    public void moveCamera(int dx, int dy) {
        xOffset += dx;
        yOffset += dy;
    }

    public void movePlayer(String username, int x, int y, double direction) {
        getPlayer(username).setPositionByServer(x, y, direction);
    }

    public void makePlayerFire(String username) {
        getPlayer(username).fire();
    }

    private Player getPlayer(String username) {
        for (var object : objects) {
            if (object.getType() == GameObject.GameObjectType.PLAYER &&
                    ((Player)object).getUsername().equals(username))
            {
                return (Player) object;
            }
        }
        throw new IllegalArgumentException("Player " + username + " doesn't exist in level");
    }

    public synchronized void update() {
        GameObject current;
        for (int i = 0; i < objects.size(); i++) {
            current = objects.get(i);
            current.update();
            if (current.isDeleted()) {
                objects.remove(current);
            }
        }
    }

    public synchronized void render(Graphics2D g) {
        g.setPaint(BG_GRADIENT);
        g.fill(new Rectangle2D.Double(0,0,Game.WIDTH,Game.HEIGHT));
        tileMap.render(g, xOffset, yOffset);
        for (var object : objects) {
            object.render(g, xOffset, yOffset);
        }
    }
}
