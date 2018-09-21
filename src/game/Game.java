package game;

import io.KeyInput;
import io.MouseHandler;
import io.MouseMotionHandler;
import net.GameClient;
import net.GameServer;
import net.packets.Packet0Login;
import net.packets.Packet1Disconnect;
import tools.FPSCounter;
import tools.ImageUtils;
import window.MainWindow;
import world.level.Level;
import world.level.TileMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Game extends Canvas {
    public static final String TITLE               = "Amazing Game";
    public static final int    WIDTH               = 960;
    public static final int    HEIGHT              = 540;
    public static final double UPDATE_RATE         = 60.0;
    public static final long   SECOND              = 1_000_000_000L;
    public static final double UPDATE_INTERVAL     = SECOND / UPDATE_RATE;

    public static final KeyInput keyInput = new KeyInput();
    public static final MouseMotionHandler mouseMotionHandler = new MouseMotionHandler();

    public static final GameServer server = createServer();
    public static final GameClient client = createClient();

    private static JFrame mainWindow;

    private boolean isRunning;
    private Thread gameThread;

    //rendering
    private static BufferStrategy bs;
    private static Graphics2D g;
    private static BufferedImage buffer;

    //game
    private Level level;

    public Game() {
        setSize(WIDTH, HEIGHT);
        mainWindow = new MainWindow(this, TITLE);

        //graphics setup
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        createBufferStrategy(3);
        bs = this.getBufferStrategy();

        //input setup
        mainWindow.add(keyInput);
        this.addMouseMotionListener(mouseMotionHandler);
        this.addMouseListener(new MouseHandler(this));

        level = new Level(TileMap.loadTileMapFromFile("testLevel.lvl"));

        client.setLevel(level);
        client.start();
        if (server != null) {
            server.start();
        }

        var player = level.getPlayer();

        //отправляем серверу данные о том, что новый игрок подключился
        Packet0Login loginPacket = new Packet0Login(player.getUsername(), player.getX(), player.getY());
        client.sendData(loginPacket.getData());

        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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
        level.update();
    }

    private void render() {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        level.render(g);
        swapBuffers();
    }

    public void mousePressed() {
        level.getPlayer().mousePressed();
    }

    private static void swapBuffers() {
        Graphics g = bs.getDrawGraphics();
        g.drawImage(buffer, 0, 0, null);
        bs.show();
    }

    private static GameServer createServer() {
        if (JOptionPane.showConfirmDialog(null,"Do you want to run a server?") == 0) {
            var server = new GameServer();
            server.setName("ServerThread");

            JOptionPane.showMessageDialog(null, "Server IP-Address: " + server.getIpAddress());

            return server;
        }
        return null;
    }

    private static GameClient createClient() {
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
        var client = new GameClient(serverIpAddress);
        client.setName("ClientThread");
        return client;
    }
}
