package net.packets;

public class Packet2InvalidConnection extends Packet {
    private String message;

    public Packet2InvalidConnection(byte[] data) {
        super(2);
        message = readData(data);
    }

    public Packet2InvalidConnection(String message) {
        super(2);
        this.message = message;
    }

    @Override
    public byte[] getData() {
        return (packetID + message).getBytes();
    }

    public String getMessage() {
        return message;
    }
}
