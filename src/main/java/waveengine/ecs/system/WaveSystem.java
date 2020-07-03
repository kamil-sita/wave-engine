package waveengine.ecs.system;


import waveengine.core.Logger;

public abstract class WaveSystem extends WaveSystemBase {

    public void initialize() {

    };

    protected void update(double deltaTime) throws Exception {

    };

    @Override
    public WaveSystem setName(String name) {
        super.setName(name);
        return this;
    }

    public final void updateIteration(double deltaTime) {
        try {
            update(deltaTime);
        } catch (Exception e) {
            Logger.getLogger().logError(getName() + ": " + e.toString() + ", " + e.getMessage());
        }
        freeComponents();
    }
}
