package net.packets;

public class Packet3Shoot extends Packet {
    private String username;

    public Packet3Shoot(byte[] data) {
        super(3);
        username = readData(data);
    }

    public Packet3Shoot(String username) {
        super(3);
        this.username = username;
    }

    @Override
    public byte[] getData() {
        return (packetID + username).getBytes();
    }

    public String getUsername() {
        return username;
    }
}
