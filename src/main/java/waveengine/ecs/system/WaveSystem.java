package waveengine.ecs.system;


import waveengine.core.Logger;

public abstract class WaveSystem extends WaveSystemBase {

    public void initialize() {

    };

    protected void update(double deltaTime) throws Exception {

    };

    public final void updateIteration(double deltaTime) {
        try {
            update(deltaTime);
        } catch (Exception e) {
            Logger.getLogger().logError(e.getMessage());
        }
        freeComponents();
    }
}
