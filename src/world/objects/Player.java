package world.objects;

import game.Game;
import net.packets.Packet3PlayerState;
import world.graphics.PlayerAnimation;
import world.level.Level;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Player extends Mob {
    private static final int PLAYER_SPEED = 7;
    public static final int PLAYER_HP = 250;
    private static final Font PLAYER_NAME_FONT = new Font("Consolas", Font.PLAIN, 18);

    private BufferedImage body;
    private BufferedImage feet;
    private Point bodySpritePosition;
    private Point feetSpritePosition;
    private double direction;
    private boolean isMoving;
    private boolean dead;

    private PlayerAnimation animation;

    private String username;
    private boolean isMainPlayer;

    private Point bulletSpawn;
    private boolean shooting;

    //new state from server
    private int newX;
    private int newY;
    private double newDirection;
    private int newHp;
    private boolean newShooting;

    public Player(Level level, int x, int y, int hp, String username, boolean isMainPlayer) {
        super(level, x, y, GameObjectType.PLAYER, PLAYER_SPEED, hp);

        this.username = username;
        this.isMainPlayer = isMainPlayer;

        animation = new PlayerAnimation();
        this.body = animation.getCurrentBodySprite();
        this.feet = animation.getCurrentFeetSprite();
        bodySpritePosition = new Point(x - body.getWidth() / 2, y - body.getHeight() / 2);
        feetSpritePosition = new Point(x - feet.getWidth() / 2, y - feet.getHeight() / 2);

        shape.width = body.getWidth() / 2;
        shape.height = body.getHeight() / 2;
        shape.x = x - shape.width / 2;
        shape.y = y - shape.height / 2;

        bulletSpawn = new Point(bodySpritePosition.x + body.getWidth(),
                bodySpritePosition.y + (body.getHeight()*3)/4);
    }

    public String getUsername() {
        return username;
    }

    public boolean isDead() {
        return dead;
    }

    void setDamage(int damage) {
        hp -= damage;
    }

    @Override
    public void update() {
        var shooting = this.shooting;
        if (isMainPlayer) moveByUser();
        else setStateFromServer();
        moveBulletSpawn();

        animation.update(isMoving);
        body = animation.getCurrentBodySprite();
        feet = animation.getCurrentFeetSprite();

        if (hp <= 0) dead = true;

        if (isMainPlayer) {
            var statePacket = new Packet3PlayerState(username, x, y, direction, hp, shooting);
            Game.client.sendData(statePacket.getData());
        }

        if (shooting) {
            new Bullet(level, bulletSpawn.x, bulletSpawn.y, direction);
            this.shooting = false;
        }

        isMoving = false;
    }

    private void moveByUser() {
        int dx = 0;
        int dy = 0;
        var mouseX = Game.mouseMotionHandler.getX();
        var mouseY = Game.mouseMotionHandler.getY();
        direction = angleTo(Game.SCREEN_CENTER_X, Game.SCREEN_CENTER_Y, mouseX, mouseY);
        if (Game.keyInput.getKey(KeyEvent.VK_W)) {
            dy--;
        }
        if (Game.keyInput.getKey(KeyEvent.VK_S)) {
            dy++;
        }
        if (Game.keyInput.getKey(KeyEvent.VK_A)) {
            dx--;
        }
        if (Game.keyInput.getKey(KeyEvent.VK_D)) {
            dx++;
        }

        if (dx != 0 || dy != 0) {
            isMoving = true;
            move(dx * speed, dy * speed);
        }
    }

    private void setStateFromServer() {
        moveByServer();
        hp = newHp;
        shooting = newShooting;
    }

    private void moveByServer() {
        if (x != newX || y != newY) {
            isMoving = true;
            x = newX;
            y = newY;
        }
        direction = newDirection;

        shape.x = x - shape.width / 2;
        shape.y = y - shape.height / 2;
        bodySpritePosition.move(x - body.getWidth() / 2, y - body.getHeight() / 2);
        feetSpritePosition.move(x - feet.getWidth() / 2, y - feet.getHeight() / 2);
    }

    @Override
    protected void move(int dx, int dy) {
        if (dx != 0 && dy != 0) {
            move(dx, 0);
            move(0, dy);
            return;
        }
        if (!hasCollided(dx, dy)) {
            x += dx;
            y += dy;
            shape.translate(dx, dy);
            bodySpritePosition.translate(dx, dy);
            feetSpritePosition.translate(dx, dy);

            level.moveCamera(dx ,dy);
        }
    }

    public void receiveStateFromServer(Packet3PlayerState state) {
        newX = state.getX();
        newY = state.getY();
        newDirection = state.getDirection();
        newHp = state.getHp();
        newShooting = state.isShooting();
    }

    private void moveBulletSpawn() {
        bulletSpawn.move(bodySpritePosition.x + body.getWidth(), bodySpritePosition.y + (body.getHeight()*3)/4);
        var newX = x + (bulletSpawn.x - x) * Math.cos(direction) - (bulletSpawn.y - y) * Math.sin(direction);
        var newY = y + (bulletSpawn.x - x) * Math.sin(direction) + (bulletSpawn.y - y) * Math.cos(direction);
        bulletSpawn.move((int)newX, (int)newY);
    }

    @Override
    public void render(Graphics2D g, int xOffset, int yOffset) {
        var bodySpriteOnScreenX = bodySpritePosition.x - xOffset;
        var bodySpriteOnScreenY = bodySpritePosition.y - yOffset;
        var feetSpriteOnScreenX = feetSpritePosition.x - xOffset;
        var feetSpriteOnScreenY = feetSpritePosition.y - yOffset;

        AffineTransform noTransform = g.getTransform();
        noTransform.rotate(0, 0, 0);

        g.rotate(direction, x - xOffset, y - yOffset);

        g.drawImage(feet, feetSpriteOnScreenX, feetSpriteOnScreenY, null);
        g.drawImage(body, bodySpriteOnScreenX, bodySpriteOnScreenY, null);

        g.setTransform(noTransform);

        g.setColor(Color.WHITE);
        g.setFont(PLAYER_NAME_FONT);
        g.drawString(username, bodySpriteOnScreenX, bodySpriteOnScreenY - 10);
        g.setColor(Color.GREEN);
        g.drawString(Integer.toString(hp), bodySpriteOnScreenX, bodySpriteOnScreenY - (10 + PLAYER_NAME_FONT.getSize()));
    }

    public void mousePressed() {
        shooting = true;
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return Math.atan2(diffY, diffX);
    }
}
