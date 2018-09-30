package net.packets;

//contains user info
public class Packet0Login extends Packet {
    private String username;
    private int x, y;
    private boolean hasInput;

    //using by server
    public Packet0Login(byte[] data) {
        super(0);
        var tokens = readData(data).split(";");
        username = tokens[0];
        x = Integer.parseInt(tokens[1]);
        y = Integer.parseInt(tokens[2]);
        hasInput = Boolean.parseBoolean(tokens[3]);
    }

    //using by client
    public Packet0Login(String username, int x, int y, boolean hasInput) {
        super(0);
        this.username = username;
        this.x = x;
        this.y = y;
        this.hasInput = hasInput;
    }

    public Packet0Login(String username) {
        this(username, 0, 0, false);
    }

    @Override
    public byte[] getData() {
        return (packetID + username + ";" + x + ";" + y + ";" + hasInput).getBytes();
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

    public boolean hasInput() {
        return hasInput;
    }
}
