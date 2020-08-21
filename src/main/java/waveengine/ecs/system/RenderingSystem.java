package waveengine.ecs.system;

import waveengine.core.Logger;
import waveengine.guiimplementation.WaveCanvasImpl;
import waveengine.guiimplementation.WaveCanvas;

public abstract class RenderingSystem extends WaveSystemBase {
    public void initialize() {

    };
    public void update(WaveCanvas canvas, double deltaTime) throws Exception {

    };
    public final void updateAndRelease(WaveCanvasImpl canvas, double deltaTime, int width, int height) {
        try {
            if (getWaveEngineRunning().getProfiler() != null) {
                getWaveEngineRunning().getProfiler().reportUps(1/deltaTime, this);
            }
            update(canvas, deltaTime);
            if (getWaveEngineRunning().getProfiler() != null) {
                getWaveEngineRunning().getProfiler().render(canvas);
            }
            canvas.renderQueue(width, height);
        } catch (Exception e) {
            Logger.getLogger().logError(e.getMessage());
            e.printStackTrace();
        }
        freeComponents();
    }
}
