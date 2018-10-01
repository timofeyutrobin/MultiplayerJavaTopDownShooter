package world.objects;

import world.level.Level;

public abstract class Mob extends GameObject {
    protected int speed;
    protected int hp;
    
    public Mob(Level level, int x, int y, GameObjectType type, int shapeWidth, int shapeHeight, int speed, int hp) {
        super(level, x, y, type, shapeWidth, shapeHeight);
        this.speed = speed;
        this.hp = hp;
    }

    public Mob(Level level, int x, int y, GameObjectType type, int speed, int hp) {
        super(level, x, y, type);
        this.speed = speed;
        this.hp = hp;
    }

    public int getHp() {
        return hp;
    }

    protected abstract void move(int dx, int dy);
}
