package world.level;

import tools.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    public enum TileTypes {
        FLOOR,
        WALL
    }

    private BufferedImage image;
    private TileTypes type;

    Tile(TileTypes type, int size) {
        this.type = type;
        BufferedImage image;
        switch (type) {
            default :
            case FLOOR :
                image = ImageUtils.loadImageFromRes("/tiles/metal_panel.png");
                this.image = ImageUtils.resize(image, size, size);
                break;
            case WALL :
                image = ImageUtils.loadImageFromRes("/tiles/wall_tile.png");
                this.image = ImageUtils.resize(image, size, size);
                break;
        }
    }

    public TileTypes getType() {
        return type;
    }

    void render(Graphics2D g, int x, int y) {
        g.drawImage(image, x, y, null);
    }
}
