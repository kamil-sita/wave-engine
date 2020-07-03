package waveengine.library.systems;

import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.ecs.system.WaveSystem;
import waveengine.library.objects.Clickable;
import waveengine.library.WaveTables;

public class ButtonStateChecker extends WaveSystem {
    @Override
    protected void update(double deltaTime) {
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
        buttonStateChecker.setName("Button State Checker");
        waveEngine.addSystem(UpdatePolicy.UPDATE_PARALLEL, buttonStateChecker, ButtonStateChecker.class);
    }

    @Override
    public String getCreator() {
        return "WAVE";
    }
}
