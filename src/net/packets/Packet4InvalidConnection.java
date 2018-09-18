package net.packets;

public class Packet4InvalidConnection extends Packet {

    public Packet4InvalidConnection() {
        super(4);
    }

    @Override
    public byte[] getData() {
        return Integer.toString(packetID).getBytes();
    }
}
