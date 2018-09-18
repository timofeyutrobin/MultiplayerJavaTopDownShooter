package net.packets;

//packet format : [id]username;x;y;direction
public class Packet2Move extends Packet {
    private String username;
    private int x, y;
    private double direction;

    public Packet2Move(byte[] data) {
        super(2);
        String[] tokens = readData(data).split(";");
        this.username = tokens[0];
        this.x = Integer.parseInt(tokens[1]);
        this.y = Integer.parseInt(tokens[2]);
        this.direction = Double.parseDouble(tokens[3]);
    }

    public Packet2Move(String username, int x, int y, double direction) {
        super(2);
        this.username = username;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    @Override
    public byte[] getData() {
        return (packetID + this.username + ";" + x + ";" + y + ";" + direction).getBytes();
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
}
