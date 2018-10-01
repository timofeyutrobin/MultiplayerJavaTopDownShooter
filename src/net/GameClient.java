package net;

import net.packets.*;
import world.level.Level;
import world.objects.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;

public class GameClient extends Thread {
    private InetAddress serverIpAddress;
    private DatagramSocket socket;
    private Level level;
    private Player mainPlayer;

    private boolean error;

    public GameClient(Level level, String serverIpAddress) {
        try {
            this.level = level;
            this.socket = new DatagramSocket();
            socket.setSoTimeout(10000);
            this.serverIpAddress = InetAddress.getByName(serverIpAddress);
        }
        catch (SocketException | UnknownHostException e) {
            showErrorAndExit("Unable to connect to server");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            }
            catch (SocketTimeoutException e) {
                showErrorAndExit("Server request timeout");
                e.printStackTrace();
            }
            catch (SocketException e) {
                break;
            }
            catch (IOException e) {
                showErrorAndExit("Unable to receive data from server. IP-Address may be incorrect");
                e.printStackTrace();
            }
            parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    public void stopClient() {
        socket.close();
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, serverIpAddress, GameServer.PORT);
        try {
            socket.send(packet);
        }
        catch (IOException e) {
            showErrorAndExit("Unable to send data to server. IP-Address may be incorrect");
            e.printStackTrace();
        }
    }

    public boolean isError() {
        return error;
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }

    private void showErrorAndExit(String message) {
        error = true;
        EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });
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
                handleLogin((Packet0Login) packet);
                break;
            case DISCONNECT :
                packet = new Packet1Disconnect(data);
                level.removePlayer(((Packet1Disconnect) packet).getUsername());
                break;
            case INVALID_CONNECTION :
                packet = new Packet2InvalidConnection(data);
                showErrorAndExit(((Packet2InvalidConnection) packet).getMessage());
            case PLAYER_STATE :
                packet = new Packet3PlayerState(data);
                handlePlayerState((Packet3PlayerState) packet);
        }
    }

    private void handleLogin(Packet0Login packet) {
        Player player = new Player(level, packet.getX(), packet.getY(), packet.getHp(), packet.getUsername(), packet.hasInput());
        if (packet.hasInput()) {
            mainPlayer = player;
            level.setCameraPosition(player.getX(), player.getY());
        }
        level.addObject(player);
        System.out.println("Player "+player.getUsername()+" has join the level");
    }

    private void handlePlayerState(Packet3PlayerState packet) {
        if (packet.getUsername().equals(mainPlayer.getUsername())) {
            return;
        }
        level.setPlayerState(packet);
    }
}
