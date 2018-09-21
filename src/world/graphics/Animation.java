package world.graphics;

import game.Game;
import tools.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Animation {
    static final Animation FEET_IDLE_ANIMATION = createAnimation("/animations/player_feet_idle.anim");
    static final Animation FEET_RUN_ANIMATION = createAnimation("/animations/player_feet_move.anim");
    static final Animation BODY_MOVE_ANIMATION = createAnimation("/animations/player_body_move.anim");
    static final Animation BODY_IDLE_ANIMATION = createAnimation("/animations/player_body_idle.anim");

    private final BufferedImage[] frames;

    private int currentFrame;
    private int updatesPerSecond;
    private int updatesCount;

    Animation(BufferedImage[] frames, int rate) {
        if (rate == 0) {
            updatesPerSecond = 0;
        }
        else {
            updatesPerSecond = (int) Game.UPDATE_RATE / rate;
        }
        if (rate > 60) {
            throw new IllegalArgumentException("Rate cannot be > 60");
        }
        if (rate < 0) {
            throw new IllegalArgumentException("Rate cannot be <= 0");
        }
        this.frames = frames;
        currentFrame = 0;
    }

    Animation(Animation toClone) {
        this.frames = toClone.frames;
        this.updatesPerSecond = toClone.updatesPerSecond;
        this.currentFrame = 0;
        this.updatesCount = 0;
    }

    void update() {
        updatesCount++;
        if (updatesCount >= updatesPerSecond) {
            currentFrame++;
            if (currentFrame == frames.length) {
                currentFrame = 0;
            }
            updatesCount = 0;
        }
    }

    BufferedImage getCurrentFrame() {
        return frames[currentFrame];
    }

    private static Animation createAnimation(String animFilePath) {
        BufferedImage[] frames = null;
        int framesCount;
        int rate = 0;
        double frameScale;
        try (InputStream is = Game.class.getResourceAsStream(animFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            rate = Integer.parseInt(reader.readLine());
            framesCount = Integer.parseInt(reader.readLine());
            frameScale = Double.parseDouble(reader.readLine());

            frames = new BufferedImage[framesCount];

            for (int i = 0; i < framesCount; i++) {
                var image = ImageUtils.loadImageFromRes(reader.readLine());
                frames[i] = ImageUtils.resize(image, (int)(image.getWidth()*frameScale), (int)(image.getHeight()*frameScale));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NumberFormatException e) {
            System.out.println("INVALID ANIM FILE FORMAT");
            e.printStackTrace();
        }
        return new Animation(frames, rate);
    }
}
