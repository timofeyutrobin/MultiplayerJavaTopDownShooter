package world.objects;

import game.Game;
import net.packets.Packet2Move;
import net.packets.Packet3Shoot;
import world.graphics.PlayerAnimation;
import world.level.Level;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.InetAddress;

public class Player extends Mob {
    private static final int PLAYER_SPEED = 7;
    private static final int PLAYER_HP = 100;
    private static final Font PLAYER_NAME_FONT = new Font("Consolas", Font.PLAIN, 18);

    private BufferedImage body;
    private BufferedImage feet;
    private Point bodySpritePosition;
    private Point feetSpritePosition;
    private double direction;
    private boolean isMoving;

    private PlayerAnimation animation;

    private String username;
    private InetAddress ipAddress;
    private int port;
    private boolean hasInput;

    private Point bulletSpawn;
    private boolean shooting;

    //new position from server
    private int newX;
    private int newY;
    private double newDirection;

    public Player(Level level, int x, int y, String username, InetAddress ipAddress, int port, boolean hasInput) {
        super(level, x, y, GameObjectType.PLAYER, PLAYER_SPEED, PLAYER_HP);

        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;

        this.hasInput = hasInput;

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    void setDamage(int damage) {
        hp -= damage;
    }

    @Override
    public void update() {
        if (hasInput) moveByUser();
        else moveByServer();
        moveBulletSpawn();

        animation.update(isMoving);
        body = animation.getCurrentBodySprite();
        feet = animation.getCurrentFeetSprite();

        if (shooting) {
            new Bullet(level, bulletSpawn.x, bulletSpawn.y, direction);

            shooting = false;
        }
        isMoving = false;
    }

    private void moveByUser() {
        int dx = 0;
        int dy = 0;
        var mouseX = Game.mouseMotionHandler.getX();
        var mouseY = Game.mouseMotionHandler.getY();
        direction = angleTo(x, y, mouseX, mouseY);
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

        Packet2Move packet = new Packet2Move(username, x, y, direction);
        Game.client.sendData(packet.getData());
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

    public void fire() {
        shooting = true;
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
        }
    }

    public void setPositionByServer(int x, int y, double direction) {
        newX = x;
        newY = y;
        newDirection = direction;
    }

    private void moveBulletSpawn() {
        bulletSpawn.move(bodySpritePosition.x + body.getWidth(), bodySpritePosition.y + (body.getHeight()*3)/4);
        var newX = x + (bulletSpawn.x - x) * Math.cos(direction) - (bulletSpawn.y - y) * Math.sin(direction);
        var newY = y + (bulletSpawn.x - x) * Math.sin(direction) + (bulletSpawn.y - y) * Math.cos(direction);
        bulletSpawn.move((int)newX, (int)newY);
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform noTransform = g.getTransform();
        noTransform.rotate(0, 0, 0);

        g.rotate(direction, x, y);

        g.drawImage(feet, feetSpritePosition.x ,feetSpritePosition.y, null);
        g.drawImage(body, bodySpritePosition.x, bodySpritePosition.y, null);

        g.setTransform(noTransform);

        g.setColor(Color.WHITE);
        g.setFont(PLAYER_NAME_FONT);
        g.drawString(username, bodySpritePosition.x, bodySpritePosition.y - 10);
        g.setColor(Color.GREEN);
        g.drawString(Integer.toString(hp), bodySpritePosition.x, bodySpritePosition.y - (10 + PLAYER_NAME_FONT.getSize()));
    }

    public void mousePressed() {
        if (hasInput) {
            fire();
            var shootingPacket = new Packet3Shoot(username);
            Game.client.sendData(shootingPacket.getData());
        }
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return Math.atan2(diffY, diffX);
    }
}
