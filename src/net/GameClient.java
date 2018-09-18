package net;

import net.packets.*;
import world.level.Level;
import world.objects.Player;

import javax.swing.*;
import java.io.IOException;
import java.net.*;

public class GameClient extends Thread {
    private InetAddress serverIpAddress;
    private DatagramSocket socket;
    private Level level;

    public GameClient(String serverIpAddress) {
        try {
            this.socket = new DatagramSocket();
            this.serverIpAddress = InetAddress.getByName(serverIpAddress);
        }
        catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public InetAddress getIpAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getPort();
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

    public void setLevel(Level level) {
        this.level = level;
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, serverIpAddress, GameServer.PORT);
        try {
            socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsePacket(byte[] data, InetAddress address, int port) {
        var message = new String(data).trim();
        Packet.PacketTypes type = Packet.lookupPacket(message.substring(0, 1));
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
                System.out.println("["+address.getHostAddress()+":"+port+"] "
                        + ((Packet1Disconnect)packet).getUsername()+" has left the world...");
                level.removePlayer(((Packet1Disconnect) packet).getUsername());
                break;
            case MOVE :
                packet = new Packet2Move(data);
                handleMove((Packet2Move) packet);
                break;
            case SHOOT :
                packet = new Packet3Shoot(data);
                handleShoot((Packet3Shoot) packet);
                break;
            case INVALID_CONNECTION :
                JOptionPane.showMessageDialog(null, "Player "+level.getPlayer().getUsername()+" is also connected");
                System.out.println(Thread.currentThread().getName());
                System.exit(0);
        }
    }

    private void handleLogin(Packet0Login packet, InetAddress address, int port) {
        System.out.println("["+address.getHostAddress()+":"+port+"] "
                + packet.getUsername()+" has joined the game...");
        Player player = new Player(level, packet.getX(), packet.getY(), packet.getUsername(), address, port, false);
        level.addObject(player);
    }

    private void handleMove(Packet2Move packet) {
        level.movePlayer(packet.getUsername(), packet.getX(), packet.getY(), packet.getDirection());
    }

    private void handleShoot(Packet3Shoot packet) {
        level.makePlayerFire(packet.getUsername());
    }
}
