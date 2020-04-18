package waveengine.library.systems;

import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.ecs.component.Semaphoring;
import waveengine.ecs.system.WaveSystem;
import waveengine.library.WaveSystems;
import waveengine.library.objects.Clickable;
import waveengine.library.WaveTables;

public class ButtonStateChecker extends WaveSystem {
    @Override
    protected void update(double deltaTime) throws Semaphoring.TableNotOwnedException {
        int x = getInteractions().getMouseX();
        int y = getInteractions().getMouseY();
        boolean isPressed = getInteractions().isMousePressed();
        var buttons = getTableFor(WaveTables.BUTTONS, Clickable.class);

        buttons.iterateReverse((integer, button) -> {
            button.checkForPress(x, y, isPressed);
        });
    }


    public static void addSelf(WaveEngine waveEngine) {
        ButtonStateChecker buttonStateChecker = new ButtonStateChecker();
        waveEngine.addSystem(WaveSystems.BUTTON_PRESS_CHECKER, UpdatePolicy.UPDATE_PARALLEL, buttonStateChecker);
    }
}
