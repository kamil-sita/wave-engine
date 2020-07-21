package waveengine.ecs.system;

import waveengine.guiimplementation.WaveCanvas;

public abstract class ProfilerSystem extends WaveSystem {

    public abstract void reportUps(double v, WaveSystemBase system);

    public abstract void render(WaveCanvas waveCanvas);
}
