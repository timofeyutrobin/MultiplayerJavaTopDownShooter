package net.packets;

//contains user info
public class Packet0Login extends Packet {
    private String username;
    private int x, y;

    //using by server
    public Packet0Login(byte[] data) {
        super(0);
        var tokens = readData(data).split(";");
        username = tokens[0];
        x = Integer.parseInt(tokens[1]);
        y = Integer.parseInt(tokens[2]);
    }

    //using by client
    public Packet0Login(String username, int x, int y) {
        super(0);
        this.username = username;
        this.x = x;
        this.y = y;
    }

    @Override
    public byte[] getData() {
        return (packetID + username + ";" + x + ";" + y).getBytes();
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
}
