package world.level;

import game.Game;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TileMap {
    //используются в файле уровня
    private static final int FLOOR = 0;
    private static final int WALL = 1;
    private static final int SPAWN = 2;

    private final Tile[] tiles;
    private final List<Rectangle> walls;
    private List<Point> spawnPoints;
    private final int tileSize;
    private final int mapWidth;

    private TileMap(int[] tileMap, int mapWidth, int tileSize) {
        this.mapWidth = mapWidth;
        this.tileSize = tileSize;

        var floorTile = new Tile(Tile.TileTypes.FLOOR, tileSize);
        var wallTile = new Tile(Tile.TileTypes.WALL, tileSize);

        walls = new ArrayList<>();
        spawnPoints = new ArrayList<>();
        tiles = new Tile[tileMap.length];
        for (int i = 0; i < tileMap.length; i++) {
            var tileXPosition = (i % mapWidth) * tileSize;
            var tileYPosition = (i / mapWidth) * tileSize;
            if (tileMap[i] == FLOOR) {
                tiles[i] = floorTile;
            }
            if (tileMap[i] == WALL) {
                tiles[i] = wallTile;
                var rect = new Rectangle(tileXPosition, tileYPosition, tileSize, tileSize);
                walls.add(rect);
            }
            if (tileMap[i] == SPAWN) {
                tiles[i] = floorTile;
                spawnPoints.add(new Point(tileXPosition + tileSize / 2, tileYPosition + tileSize / 2));
            }
        }
    }

    void render(Graphics2D g, int xOffset, int yOffset) {
        for (int i = 0; i < tiles.length; i++) {
            int tileScreenX = ((i % mapWidth) * tileSize) - xOffset;
            int tileScreenY = ((i / mapWidth) * tileSize) - yOffset;

            //рисуются только тайлы в пределах экрана
            if (tileScreenX < -tileSize || tileScreenX > Game.WIDTH || tileScreenY < -tileSize || tileScreenY > Game.HEIGHT) {
                continue;
            }
            tiles[i].render(g, tileScreenX, tileScreenY);
        }
    }

    public List<Rectangle> getWalls() {
        return walls;
    }

    public List<Point> getSpawnPoints() {
        return spawnPoints;
    }

    public static TileMap loadTileMapFromFile(String levelFileName) {
        try (InputStream is = Game.class.getResourceAsStream("/levels/" + levelFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            var tileSize = Integer.parseInt(reader.readLine());
            var width = Integer.parseInt(reader.readLine());
            var height = Integer.parseInt(reader.readLine());
            var tiles = new int[width * height];

            String delimiter = "\\s+";
            for (int y = 0; y < height; y++) {
                String line = reader.readLine();
                String[] tokens = line.split(delimiter);
                for (int x = 0; x < width; x++) {
                    tiles[y * width + x] = Integer.parseInt(tokens[x]);
                }
            }
            return new TileMap(tiles, width, tileSize);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NumberFormatException e) {
            System.out.println("INVALID LEVEL FILE FORMAT");
            e.printStackTrace();
        }
        throw new IllegalArgumentException("UNABLE TO LOAD TILEMAP FROM FILE: /levels/" + levelFileName);
    }
}
