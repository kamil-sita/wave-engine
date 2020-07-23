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
            if (getWaveEngineRunning().getProfiler() != null) {
                getWaveEngineRunning().getProfiler().reportUps(1/deltaTime, this);
            }
            long time = System.nanoTime();
            update(canvas, deltaTime);
            System.out.println("iter:" + (System.nanoTime() - time) / 1_000_000.0 );
            if (getWaveEngineRunning().getProfiler() != null) {
                getWaveEngineRunning().getProfiler().render(canvas);
            }
            time = System.nanoTime();
            canvas.renderQueue();
            System.out.println("rend:" + (System.nanoTime() - time) / 1_000_000.0 );
        } catch (Exception e) {
            Logger.getLogger().logError(e.getMessage());
            e.printStackTrace();
        }
        freeComponents();
    }
}
