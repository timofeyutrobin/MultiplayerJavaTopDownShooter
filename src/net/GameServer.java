package net;

import net.packets.*;
import world.level.Level;
import world.objects.Player;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class GameServer extends Thread {
    static final int PORT = 1331;

    private DatagramSocket socket;
    private Level level;
    private List<Player> connectedPlayers;
    private String ipAddress;

    public GameServer() {
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

    public void setLevel(Level level) {
        this.level = level;
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
                System.out.println("["+address.getHostAddress()+":"+port+"] "
                        + ((Packet0Login)packet).getUsername()+" has connected...");
                handleLogin((Packet0Login) packet, address, port);
                break;
            case DISCONNECT :
                packet = new Packet1Disconnect(data);
                System.out.println("["+address.getHostAddress()+":"+port+"] "
                        + ((Packet1Disconnect)packet).getUsername()+" has left...");
                removeConnection((Packet1Disconnect) packet);
                break;
            case MOVE :
                packet = new Packet2Move(data);
                handleMove((Packet2Move)packet);
                break;
            case SHOOT :
                packet = new Packet3Shoot(data);
                handleShoot((Packet3Shoot) packet);
                break;
        }
    }

    private void handleLogin(Packet0Login packet, InetAddress ipAddress, int port) {
        Player player = new Player(level, packet.getX(), packet.getY(), packet.getUsername(), ipAddress, port, false);
        addConnection(player, packet);
    }

    private void handleMove(Packet2Move packet) {
        getPlayer(packet.getUsername()).moveAbs(packet.getX(), packet.getY(), packet.getDirection());
        sendDataToAllClients(packet.getData());
    }

    private void handleShoot(Packet3Shoot packet) {
        sendDataToAllClients(packet.getData());
    }

    public void sendData(byte[] data, InetAddress ipAddress, int port) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDataToAllClients(byte[] data) {
        for (var player : connectedPlayers) {
            sendData(data, player.getIpAddress(), player.getPort());
        }
    }

    private void removeConnection(Packet1Disconnect packet) {
        sendDataToAllClients(packet.getData());
        connectedPlayers.remove(getPlayer(packet.getUsername()));
    }

    private Player getPlayer(String username) {
        for (var player : connectedPlayers) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + username + " doesn't exist");
    }

    private void addConnection(Player player, Packet0Login packet) {
        boolean alreadyConnected = false;
        for (var p : connectedPlayers) {
            if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
                alreadyConnected = true;
                var invalidConnectionPacket = new Packet4InvalidConnection();
                sendData(invalidConnectionPacket.getData(), player.getIpAddress(), player.getPort());
            }
            else {
                //отправляем каждому игроку данные о том, что новый игрок подключился к серверу
                sendData(packet.getData(), p.getIpAddress(), p.getPort());

                //отправляем новому игроку данные обо всех игроках на карте
                packet = new Packet0Login(p.getUsername(), p.getX(), p.getY());
                sendData(packet.getData(), player.getIpAddress(), player.getPort());
            }
        }
        if (!alreadyConnected) {
            connectedPlayers.add(player);
        }
    }
}