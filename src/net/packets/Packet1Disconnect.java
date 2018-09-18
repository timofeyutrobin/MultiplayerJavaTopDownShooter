package net.packets;

public class Packet1Disconnect extends Packet {
    private String username;

    public Packet1Disconnect(byte[] data) {
        super(1);
        this.username = readData(data);
    }

    public Packet1Disconnect(String username) {
        super(1);
        this.username = username;
    }

    @Override
    public byte[] getData() {
        return (packetID + this.username).getBytes();
    }

    public String getUsername() {
        return username;
    }
}
