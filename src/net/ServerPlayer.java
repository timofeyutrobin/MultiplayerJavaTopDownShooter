package net;

import net.packets.Packet3PlayerState;

import java.net.InetAddress;

class ServerPlayer {
    int x, y;
    double direction;
    int hp;
    boolean shooting;
    String username;
    InetAddress ipAddress;
    int port;

    ServerPlayer(int x, int y, int hp, String username, InetAddress ipAddress, int port) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    void setState(Packet3PlayerState packet) {
        this.x = packet.getX();
        this.y = packet.getY();
        this.direction = packet.getDirection();
        this.hp = packet.getHp();
        this.shooting = packet.isShooting();
    }
}
