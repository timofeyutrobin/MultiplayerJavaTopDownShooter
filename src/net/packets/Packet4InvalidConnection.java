package net.packets;

public class Packet4InvalidConnection extends Packet {
    private String message;

    public Packet4InvalidConnection(byte[] data) {
        super(4);
        message = readData(data);
    }

    public Packet4InvalidConnection(String message) {
        super(4);
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
