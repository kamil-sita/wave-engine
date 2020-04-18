package waveengine.core;

import waveengine.ecs.system.WaveSystem;

public interface SchedulerImplementation {
    void start();

    void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy);
}
