package waveengine.ecs.system;

import waveengine.core.Logger;
import waveengine.guiimplementation.WaveCanvasImpl;
import waveengine.guiimplementation.WaveCanvas;

public abstract class RenderingSystem extends WaveSystemBase {
    public void initialize() {

    };
    public void update(WaveCanvas canvas, double deltaTime) throws Exception {

    };
    public final void updateAndRelease(WaveCanvasImpl canvas, double deltaTime) {
        try {
            update(canvas, deltaTime);
            canvas.renderQueue();
        } catch (Exception e) {
            Logger.getLogger().logError(e.getMessage());
            e.printStackTrace();
        }
        freeComponents();
    }
}
