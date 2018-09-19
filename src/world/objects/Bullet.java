package world.objects;

import world.level.Level;

import java.awt.*;

public class Bullet extends GameObject {
    public static final int BULLET_SIZE = 6;

    public static final int BULLET_DAMAGE = 5;
    public static final int BULLET_SPEED = 25;

    private int damage;
    private int speed;
    private double dx, dy;


    public Bullet(Level level, int x, int y, double direction) {
        super(level, x, y, GameObjectType.BULLET, BULLET_SIZE, BULLET_SIZE);
        this.damage = BULLET_DAMAGE;
        this.speed = BULLET_SPEED;

        dx = speed * Math.cos(direction);
        dy = speed * Math.sin(direction);

        level.addObject(this);
    }

    @Override
    public void update() {
        if (hasCollided((int)dx, (int)dy)) {
            this.delete();
        }
        else {
            x += dx;
            y += dy;
            shape.x += dx;
            shape.y += dy;
        }
    }

    @Override
    protected boolean hasCollided(int dx, int dy) {
        for (var wall : level.getTileMap().getWalls()) {
            if (wall.intersects(shape.x + dx, shape.y + dy, shape.width, shape.height)) {
                return true;
            }
        }
        for (var object : level.getObjects()) {
            if (this == object) continue;
            if (object.shape.intersects(shape.x + dx, shape.y + dy, shape.width, shape.height)) {
                if (object.getType() == GameObjectType.PLAYER) {
                    ((Player) object).setDamage(this.damage);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillOval(shape.x, shape.y, BULLET_SIZE, BULLET_SIZE);
    }
}
