package waveengine.core;

import waveengine.ecs.system.WaveSystem;

public class Scheduler {

    private final WaveEngineRunning waveEngineRunning;
    private final SchedulerImplementation schedulerImplementation;

    public Scheduler(WaveEngineRunning waveEngineRunning) {
        if (waveEngineRunning.getWaveEngineParameters().singleThreaded()) {
            schedulerImplementation = new SchedulerSingleThread(waveEngineRunning);
        } else {
            schedulerImplementation = new SchedulerMultiThread(waveEngineRunning);
        }


        this.waveEngineRunning = waveEngineRunning;
    }

    void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        schedulerImplementation.addSystem(waveSystem, updatePolicy);
    }

    void start() {
        schedulerImplementation.start();
    }

}
