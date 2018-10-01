package net;

import net.packets.*;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class GameServer extends Thread {
    static final int PORT = 1331;
    static final int PLAYER_LIMIT = 4;

    private DatagramSocket socket;
    private List<ServerPlayer> connectedPlayers;
    private List<Point> spawnPoints;
    private String ipAddress;

    public GameServer(List<Point> spawnPoints) {
        if (spawnPoints.size() < PLAYER_LIMIT)
            throw new IllegalArgumentException("Not enough spawn points");
        this.spawnPoints = spawnPoints;
        this.connectedPlayers = new ArrayList<>();
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
            this.socket = new DatagramSocket(PORT);
        }
        catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public void run() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    private void parsePacket(byte[] data, InetAddress address, int port) {
        var message = new String(data).trim();
        Packet.PacketTypes type = Packet.lookupPacket(message);

        Packet packet;
        switch (type) {
            default :
            case INVALID :
                break;
            case LOGIN :
                packet = new Packet0Login(data);
                handleLogin((Packet0Login) packet, address, port);
                break;
            case DISCONNECT :
                packet = new Packet1Disconnect(data);
                removeConnection((Packet1Disconnect) packet);
                break;
            case PLAYER_STATE:
                packet = new Packet3PlayerState(data);
                handlePlayerState((Packet3PlayerState) packet);
        }
    }

    private void handleLogin(Packet0Login packet, InetAddress ipAddress, int port) {
        if (connectedPlayers.size() >= PLAYER_LIMIT) {
            var invalidConnectionPacket = new Packet2InvalidConnection("Server is full");
            sendData(invalidConnectionPacket.getData(), ipAddress, port);
        }
        else {
            var playerSpawnPosition = getCurrentSpawnPoint();

            var loginPacketBack = new Packet0Login(packet.getUsername(),
                    playerSpawnPosition.x, playerSpawnPosition.y, packet.getHp(), true);
            sendData(loginPacketBack.getData(), ipAddress, port);

            var loginPacketNew = new Packet0Login(packet.getUsername(),
                    playerSpawnPosition.x, playerSpawnPosition.y, packet.getHp(), false);
            var player = new ServerPlayer(playerSpawnPosition.x, playerSpawnPosition.y, packet.getHp(),
                    packet.getUsername(), ipAddress, port);

            addConnection(player, loginPacketNew);
        }
    }

    private void handlePlayerState(Packet3PlayerState packet) {
        getPlayer(packet.getUsername()).setState(packet);
        sendDataToAllClients(packet.getData());
    }

    private void sendData(byte[] data, InetAddress ipAddress, int port) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDataToAllClients(byte[] data) {
        for (var player : connectedPlayers) {
            sendData(data, player.ipAddress, player.port);
        }
    }

    private void removeConnection(Packet1Disconnect packet) {
        connectedPlayers.remove(getPlayer(packet.getUsername()));
        sendDataToAllClients(packet.getData());
    }

    private ServerPlayer getPlayer(String username) {
        for (var player : connectedPlayers) {
            if (player.username.equals(username)) {
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + username + " doesn't exist");
    }

    private Point getCurrentSpawnPoint() {
        if (connectedPlayers.isEmpty())
            return spawnPoints.get(0);

        var spawnPointIndex = 0;
        var maxDist = 0.0;
        var min = new double[spawnPoints.size()];
        for (int i = 0; i < spawnPoints.size(); i++) {
            var x = spawnPoints.get(i).x;
            var y = spawnPoints.get(i).y;
            min[i] = Integer.MAX_VALUE;
            for (var player : connectedPlayers) {
                var dist = Math.abs(x - player.x) + Math.abs(y - player.y);
                if (dist < min[i])
                    min[i] = dist;
            }
            if (min[i] >= maxDist) {
                maxDist = min[i];
                spawnPointIndex = i;
            }
        }
        return spawnPoints.get(spawnPointIndex);
    }

    private void addConnection(ServerPlayer player, Packet0Login packet) {
        boolean alreadyConnected = false;
        for (var p : connectedPlayers) {
            if (player.username.equalsIgnoreCase(p.username)) {
                alreadyConnected = true;
                var invalidConnectionPacket = new Packet2InvalidConnection("Player"+player.username+"is also connected");
                sendData(invalidConnectionPacket.getData(), player.ipAddress, player.port);
            }
            else {
                //отправляем каждому игроку данные о том, что новый игрок подключился к серверу
                sendData(packet.getData(), p.ipAddress, p.port);

                //отправляем новому игроку данные обо всех игроках на карте
                var packetCurrentPlayer = new Packet0Login(p.username, p.x, p.y, p.hp, false);
                sendData(packetCurrentPlayer.getData(), player.ipAddress, player.port);
            }
        }
        if (!alreadyConnected) {
            connectedPlayers.add(player);
        }
    }
}
