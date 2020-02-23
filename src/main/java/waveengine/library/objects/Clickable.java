package waveengine.library.objects;

import waveengine.Discriminator;
import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.ecs.entity.Entity;
import waveengine.library.systems.ButtonStateChecker;
import waveengine.library.WaveLibSystemDiscriminator;

public class Clickable {

    private int x;
    private int y;
    private int width;
    private int height;
    private Runnable onButtonPressed;

    public Clickable(int x, int y, int width, int height, Runnable onButtonPressed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onButtonPressed = onButtonPressed;
    }

    public boolean isInButton(int xPos, int yPos) {
        return xPos >= x && x <= xPos + width && yPos >= y && y <= yPos + height;
    }

    int state = STATE_NOT_LISTENING;
    private static final int STATE_NOT_LISTENING = 0;
    private static final int STATE_PRESSED_ON = 1;

    public void checkForPress(int xPos, int yPos, boolean pressed) {
        boolean mouseInside = isInButton(xPos, yPos);

        if (state == STATE_NOT_LISTENING) {
            if (mouseInside && pressed) {
                state = STATE_PRESSED_ON;
            }
        }

        if (state == STATE_PRESSED_ON) {
            if (!mouseInside && !pressed) {
                state = STATE_NOT_LISTENING;
            }

            if (mouseInside && !pressed) {
                onButtonPressed.run();
                state = STATE_NOT_LISTENING;
            }
        }
    }


    public void addSelf(WaveEngine waveEngine, Discriminator activeStage) {
        waveEngine.addSystem(WaveLibSystemDiscriminator.BUTTON_PRESS_CHECKER, UpdatePolicy.UPDATE_PARALLEL, new ButtonStateChecker());

        var entity = Entity.getEntityActiveOnOneStage(activeStage);
        var manager = waveEngine.getComponentManager();

        manager.addEntityToComponent(entity, );
    }
}
