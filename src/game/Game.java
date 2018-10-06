package game;

import io.KeyInput;
import io.MouseHandler;
import io.MouseMotionHandler;
import net.GameClient;
import net.GameServer;
import net.packets.Packet0Login;
import net.packets.Packet1Disconnect;
import tools.FPSCounter;
import window.MainWindow;
import world.level.Level;
import world.level.TileMap;
import world.objects.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.List;

public class Game extends Canvas {
    public static final String TITLE               = "Amazing Game";
    public static final int    WIDTH               = 960;
    public static final int    HEIGHT              = 540;
    public static final int    SCREEN_CENTER_X     = WIDTH / 2;
    public static final int    SCREEN_CENTER_Y     = HEIGHT / 2;
    public static final double UPDATE_RATE         = 60.0;
    public static final long   SECOND              = 1_000_000_000L;
    public static final double UPDATE_INTERVAL     = SECOND / UPDATE_RATE;

    public static final KeyInput keyInput = new KeyInput();
    public static final MouseMotionHandler mouseMotionHandler = new MouseMotionHandler();

    public static GameClient client;
    private static GameServer server;

    private static JFrame mainWindow;

    private boolean isRunning;
    private Thread gameThread;

    //rendering
    private static BufferStrategy bs;
    private static Graphics2D g;
    private static BufferedImage buffer;

    //game
    private final Level level;
    private final Player player;

    public Game() {
        setSize(WIDTH, HEIGHT);
        mainWindow = new MainWindow(this, TITLE);

        //graphics setup
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        createBufferStrategy(3);
        bs = this.getBufferStrategy();

        level = new Level(TileMap.loadTileMapFromFile("testLevel.lvl"));
        createServer(level.getTileMap().getSpawnPoints());
        createClient(level);

        var username = JOptionPane.showInputDialog("Enter your username");
        if (username == null) {
            System.exit(0);
        }

        client.start();
        if (server != null) {
            server.start();
        }

        var loginPacket = new Packet0Login(username);
        client.sendData(loginPacket.getData());

        //ждем, пока клиент создаст игрока в правильном месте карты или получит сообщение об ошибке
        while (client.getMainPlayer() == null && !client.isError()) {
            Thread.yield();
        }

        player = client.getMainPlayer();

        //input setup
        mainWindow.add(keyInput);
        this.addMouseMotionListener(mouseMotionHandler);
        this.addMouseListener(new MouseHandler(this));

        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                level.getObjects().remove(player);
                Packet1Disconnect packet = new Packet1Disconnect(player.getUsername());
                client.sendData(packet.getData());
            }
        });

        setVisible(true);
        mainWindow.setVisible(true);
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        gameThread = new Thread(() -> {
            boolean render;
            long previousTime = System.nanoTime();
            double delta = 0;
            long fpsCounterTime = 0;

            //game loop
            while (isRunning) {
                var currentTime = System.nanoTime();
                var elapsedTime = currentTime - previousTime;
                previousTime = currentTime;
                fpsCounterTime += elapsedTime;

                render = false;
                boolean updateLoop = false;
                delta += (elapsedTime / UPDATE_INTERVAL);

                while (delta >= 1) {
                    update();
                    render = true;
                    FPSCounter.incrementUpdates();
                    delta--;
                    //Считаем, сколько раз пытались догнать update
                    //Если updLoops > 0, значит компьютер не успевает делать update
                    if (updateLoop) {
                        FPSCounter.incrementUpdateLoops();
                    }
                    else {
                        updateLoop = true;
                    }
                }
                if (render) {
                    render();
                    FPSCounter.incrementFrames();
                }
                if (fpsCounterTime >= SECOND) {
                    FPSCounter.writeStatistics();
                    mainWindow.setTitle(TITLE + " | " + FPSCounter.getStatistics());
                    fpsCounterTime = 0;
                }
            }
        });
        gameThread.setName("GameThread");
        gameThread.start();
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        if (player.isDead()) {
            EventQueue.invokeLater(() -> {
                var disconnectPacket = new Packet1Disconnect(player.getUsername());
                client.sendData(disconnectPacket.getData());
                client.stopClient();
                mainWindow.dispose();
                String message;
                if (server != null) message = "You died. Press OK to close the server.";
                else message = "You died";
                JOptionPane.showMessageDialog(null, message);
                System.exit(0);
            });
            stop();
        }
        level.update();
    }

    private void render() {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        level.render(g);
        swapBuffers();
    }

    public void mousePressed() {
        player.mousePressed();
    }

    private static void swapBuffers() {
        Graphics g = bs.getDrawGraphics();
        g.drawImage(buffer, 0, 0, null);
        bs.show();
    }

    private void createServer(List<Point> spawnPoints) {
        if (JOptionPane.showConfirmDialog(null,"Do you want to run a server?") == 0) {
            server = new GameServer(spawnPoints);
            server.setName("ServerThread");

            JOptionPane.showMessageDialog(null, "Server IP-Address: " + server.getIpAddress());
        }
    }

    private void createClient(Level level) {
        String serverIpAddress;
        if (server != null) {
            serverIpAddress = server.getIpAddress();
        }
        else {
            serverIpAddress = JOptionPane.showInputDialog("Enter the server IP-Address");
            if (serverIpAddress == null) {
                System.exit(0);
            }
        }
        client = new GameClient(level, serverIpAddress);
        client.setName("ClientThread");
    }
}
