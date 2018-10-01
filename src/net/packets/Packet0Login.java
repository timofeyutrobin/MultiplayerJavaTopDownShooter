package net.packets;

import world.objects.Player;

//contains user info
public class Packet0Login extends Packet {
    private String username;
    private int x, y;
    private int hp;
    private int hasInput;

    //using by server
    public Packet0Login(byte[] data) {
        super(0);
        var tokens = readData(data).split(";");
        username = tokens[0];
        x = Integer.parseInt(tokens[1]);
        y = Integer.parseInt(tokens[2]);
        hp = Integer.parseInt(tokens[3]);
        hasInput = Integer.parseInt(tokens[4]);
    }

    //using by client
    public Packet0Login(String username, int x, int y, int hp, boolean hasInput) {
        super(0);
        this.username = username;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.hasInput = hasInput ? 1 : 0;
    }

    public Packet0Login(String username) {
        this(username, 0, 0, Player.PLAYER_HP, false);
    }

    @Override
    public byte[] getData() {
        return (packetID + username + ";" + x + ";" + y + ";" + hp + ";" + hasInput).getBytes();
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

    public int getHp() {
        return hp;
    }

    public boolean hasInput() {
        return hasInput == 1;
    }
}
