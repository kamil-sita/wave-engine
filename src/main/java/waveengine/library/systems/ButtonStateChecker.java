package waveengine.library.systems;

import waveengine.ecs.system.WaveSystem;
import waveengine.library.objects.Clickable;
import waveengine.library.WaveLibObjectDiscriminator;

public class ButtonStateChecker extends WaveSystem {
    @Override
    protected void update(double deltaTime) {
        int x = getInteractions().getMouseX();
        int y = getInteractions().getMouseY();
        boolean isPressed = getInteractions().isMouseClicked();

        var buttons = getTableFor(WaveLibObjectDiscriminator.BUTTONS);

        buttons.iterate(true, (integer, object) -> {
            var button = (Clickable) object;
            button.checkForPress(x, y, isPressed);
        });
    }
}
