package waveengine.ecs.system;

import waveengine.core.Logger;
import waveengine.guiimplementation.WaveCanvas;

public abstract class RenderingSystem extends WaveSystemBase {
    public void initialize() {

    };
    public void update(WaveCanvas canvas, double deltaTime) throws Exception {

    };
    public final void updateAndRelease(WaveCanvas canvas, double deltaTime) {
        try {
            update(canvas, deltaTime);
        } catch (Exception e) {
            Logger.getLogger().logError(e.getMessage());
            e.printStackTrace();
        }
        freeComponents();
    }
}
