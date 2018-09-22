package tools;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

public abstract class ImageUtils {

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        var newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        newImage.getGraphics().drawImage(image, 0, 0, width, height, null);
        return newImage;
    }

    public static BufferedImage copyImage(BufferedImage source) {
        var newImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        var g = newImage.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public static BufferedImage loadImageFromRes(String path) {
        BufferedImage image = null;
        try (InputStream is = ImageUtils.class.getResourceAsStream(path)) {
            image = ImageIO.read(is);
        }
        catch (IOException e) {
            System.out.println("UNABLE TO LOAD IMAGE FROM RES DIRECTORY");
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage loadImageFromFile(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
