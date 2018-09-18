package world.objects;

import world.level.Level;

import java.awt.*;

public abstract class GameObject {
    public enum GameObjectType {
        PLAYER,
        BULLET
    }

    protected Level level;
    protected int x, y;
    protected GameObjectType type;
    protected Rectangle shape;

    private boolean deleted;

    public GameObject(Level level, int x, int y, GameObjectType type) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.type = type;
        shape = new Rectangle(x, y, 0, 0);
    }

    public GameObject(Level level, int x, int y, GameObjectType type, int shapeWidth, int shapeHeight) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.type = type;
        shape = new Rectangle(x - shapeWidth / 2, y - shapeHeight / 2, shapeWidth, shapeHeight);
    }

    public GameObjectType getType() {
        return type;
    }

    public abstract void update();
    public abstract void render(Graphics2D g);

    protected boolean hasCollided(int dx, int dy) {
        for (var wall : level.getTileMap().getWalls()) {
            if (wall.intersects(shape.x + dx, shape.y + dy, shape.width, shape.height)) {
                return true;
            }
        }
        for (var object : level.getObjects()) {
            if (this == object) continue;
            if (object.shape.intersects(shape.x + dx, shape.y + dy, shape.width, shape.height)) {
                return true;
            }
        }
        return false;
    }

    public void delete() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
