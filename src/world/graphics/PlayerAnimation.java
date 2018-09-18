package world.graphics;

import java.awt.image.BufferedImage;

public class PlayerAnimation {
    private final Animation feetIdle;
    private final Animation feetRun;
    private final Animation bodyIdle;
    private final Animation bodyRun;

    private BufferedImage currentBodySprite;
    private BufferedImage currentFeetSprite;

    public PlayerAnimation(Animation feetRun, Animation bodyIdle, Animation bodyRun, Animation feetIdle) {
        this.feetRun = feetRun;
        this.bodyIdle = bodyIdle;
        this.bodyRun = bodyRun;
        this.feetIdle = feetIdle;

        currentBodySprite = bodyIdle.getCurrentFrame();
        currentFeetSprite = feetIdle.getCurrentFrame();
    }

    public PlayerAnimation() {
        this(Animation.FEET_RUN_ANIMATION, Animation.BODY_IDLE_ANIMATION,
                Animation.BODY_MOVE_ANIMATION, Animation.FEET_IDLE_ANIMATION);
    }

    public void update(boolean isMoving) {
        if (!isMoving) {
            bodyIdle.update();
            feetIdle.update();
            currentBodySprite = bodyIdle.getCurrentFrame();
            currentFeetSprite = feetIdle.getCurrentFrame();
        }
        else {
            bodyRun.update();
            feetRun.update();
            currentBodySprite = bodyRun.getCurrentFrame();
            currentFeetSprite = feetRun.getCurrentFrame();
        }
    }

    public BufferedImage getCurrentBodySprite() {
        return currentBodySprite;
    }

    public BufferedImage getCurrentFeetSprite() {
        return currentFeetSprite;
    }
}
