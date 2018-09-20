package world.level;

import game.Game;
import world.objects.GameObject;
import world.objects.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private final TileMap tileMap;
    private final ArrayList<GameObject> objects;

    private final Player player;

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
    }

    public synchronized List<GameObject> getObjects() {
        return objects;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public synchronized void addObject(GameObject object) {
        objects.add(object);
    }

    public void removePlayer(String username) {
        for (var object : objects) {
            if (object.getType() == GameObject.GameObjectType.PLAYER
                    && ((Player) object).getUsername().equals(username))
            {
                object.delete();
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void movePlayer(String username, int x, int y, double direction) {
        getPlayer(username).setPositionByServer(x, y, direction);
    }

    public void makePlayerFire(String username) {
        getPlayer(username).fire();
    }

    private Player getPlayer(String username) {
        for (var object : getObjects()) {
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
        tileMap.render(g);
        for (var object : objects) {
            object.render(g);
        }
    }
}
