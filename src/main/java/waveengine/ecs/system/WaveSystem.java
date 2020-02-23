package waveengine.ecs.system;


public abstract class WaveSystem extends WaveSystemBase {

    public void initialize() {

    };

    protected void update(double deltaTime) {

    };

    public final void updateIteration(double deltaTime) {
        update(deltaTime);
        freeComponents();
    }
}
