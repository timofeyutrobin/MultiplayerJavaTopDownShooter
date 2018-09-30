package net;

import java.net.InetAddress;

class ServerPlayer {
    int x, y;
    String username;
    InetAddress ipAddress;
    int port;

    ServerPlayer(int x, int y, String username, InetAddress ipAddress, int port) {
        this.x = x;
        this.y = y;
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    void move(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
