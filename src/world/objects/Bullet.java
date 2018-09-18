package world.objects;

import world.level.Level;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Bullet extends GameObject {
    public static final int BULLET_WIDTH = 15;
    public static final int BULLET_HEIGHT = 5;

    public static final int BULLET_DAMAGE = 5;
    public static final int BULLET_SPEED = 25;

    private Point spritePosition;

    private int damage;
    private int speed;
    private double direction;
    private double dx, dy;


    // TODO: 12.09.2018 Неправильно двигается пуля
    public Bullet(Level level, int x, int y, double direction) {
        super(level, x, y, GameObjectType.BULLET, BULLET_HEIGHT, BULLET_HEIGHT);
        this.damage = BULLET_DAMAGE;
        this.speed = BULLET_SPEED;
        this.direction = direction;

        dx = speed * Math.cos(direction);
        dy = speed * Math.sin(direction);

        spritePosition = new Point(x - BULLET_WIDTH / 2, y - BULLET_HEIGHT / 2);

        level.addObject(this);
    }

    @Override
    public void update() {
        if (hasCollided((int)dx, (int)dy)) {
            //collision = true;
            this.delete();
        }
        else {
            x += dx;
            y += dy;
            shape.x += (int)dx + 0.5;
            shape.y += (int)dy + 0.5;
            spritePosition.translate((int)(dx + 0.5), (int)(dy + 0.5));
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
        AffineTransform noTransform = g.getTransform();

        g.rotate(direction, x, y);
        g.setColor(Color.WHITE);
        g.fillRect(spritePosition.x, spritePosition.y, BULLET_WIDTH, BULLET_HEIGHT);

        g.setTransform(noTransform);

        g.setColor(Color.RED);
        g.drawRect(shape.x, shape.y, shape.width, shape.height);
    }
}
