package waveengine.ecs.system;



public abstract class WaveSystem extends WaveSystemBase {

    public void initialize() {

    };

    protected void update(double deltaTime) {

    };

    @Override
    public WaveSystem setName(String name) {
        super.setName(name);
        return this;
    }

    public final void updateIteration(double deltaTime) {
        getWaveEngineRunning().getProfiler().reportUps(1/deltaTime, this);
        update(deltaTime);
        freeComponents();
    }
}
