package net.packets;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Packet3PlayerState extends Packet {
    private String username;
    private int x, y;
    private double direction;
    private int hp;
    private int shooting; //0 - false 1 - true

    public Packet3PlayerState(byte[] data) {
        super(3);
        var tokens = readData(data).split(";");
        username = tokens[0];
        x = Integer.parseInt(tokens[1]);
        y = Integer.parseInt(tokens[2]);
        direction = Double.parseDouble(tokens[3]);
        hp = Integer.parseInt(tokens[4]);
        shooting = Integer.parseInt(tokens[5]);
    }

    public Packet3PlayerState(String username, int x, int y, double direction, int hp, boolean isShooting) {
        super(3);
        this.username = username;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.hp = hp;
        this.shooting = isShooting ? 1 : 0;
    }

    private Iterable<String> strings() {
        return Arrays.stream(getClass().getDeclaredFields())
                .map(e -> {
                    try {
                        return String.valueOf(e.get(this));
                    }
                    catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList());
    }

    @Override
    public byte[] getData() {
        return (packetID + String.join(";", strings())).getBytes();
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

    public double getDirection() {
        return direction;
    }

    public int getHp() {
        return hp;
    }

    public boolean isShooting() {
        return shooting == 1;
    }
}
