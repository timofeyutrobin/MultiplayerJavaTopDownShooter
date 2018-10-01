package net.packets;

//первый байт - идентификатор пакета, описаны в enum PacketTypes
public abstract class Packet {
    public enum PacketTypes {
        INVALID(-1),
        LOGIN(0),
        DISCONNECT(1),
        INVALID_CONNECTION(2),
        PLAYER_STATE(3);

        private int packetID;

        PacketTypes(int packetID) {
            this.packetID = packetID;
        }

        public int getID() {
            return packetID;
        }
    }

    protected int packetID;

    Packet(int packetID) {
        this.packetID = packetID;
    }

    public abstract byte[] getData();

    String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(1);
    }

    public static PacketTypes lookupPacket(String packetDataString) {
        try {
            return lookupPacket(Integer.parseInt(packetDataString.substring(0, 1)));
        }
        catch (NumberFormatException e) {
            return PacketTypes.INVALID;
        }
    }

    private static PacketTypes lookupPacket(int id) {
        for (var p : PacketTypes.values()) {
            if (p.getID() == id) {
                return p;
            }
        }
        return PacketTypes.INVALID;
    }
}
