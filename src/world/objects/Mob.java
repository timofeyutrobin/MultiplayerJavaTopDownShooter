package world.objects;

import world.level.Level;

public abstract class Mob extends GameObject {
    protected int speed;
    protected int hp;
    
    public Mob(Level level, int x, int y, GameObjectType type, int shapeWidth, int shapeHeight, int speed, int hp) {
        super(level, x, y, type, shapeWidth, shapeHeight);
        /*for (var wall : level.getTileMap().getWalls()) {
            if (wall.intersects(x, y, shapeWidth, shapeHeight)) {
                throw new IllegalArgumentException("Mob intersects the wall!");
            }
        }*/
        this.speed = speed;
        this.hp = hp;
    }

    public Mob(Level level, int x, int y, GameObjectType type, int speed, int hp) {
        super(level, x, y, type);
        this.speed = speed;
        this.hp = hp;
    }

    protected abstract void move(int dx, int dy);

    public int getHealth() {
        return hp;
    }

    /*protected void move(int dx, int dy) {
        if (dx != 0 && dy != 0) {
            move(dx, 0);
            move(0, dy);
            return;
        }
        if (!hasCollided(dx * speed, dy * speed)) {
            x += dx * speed;
            y += dy * speed;
            shape.translate(dx * speed, dy * speed);
        }
    }*/
}
