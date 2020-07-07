package waveengine.core;

import waveengine.ecs.system.WaveSystem;

import java.util.ArrayList;
import java.util.List;

public class SchedulerSingleThread implements SchedulerImplementation {

    private final WaveEngineRunning waveEngineRunning;
    private final List<WaveSystem> update = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();
    private boolean started = false;

    public SchedulerSingleThread(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        Logger.getLogger().logInfo("Single threaded scheduler created");
    }

    @Override
    public void start() {
        started = true;
        Logger.getLogger().logInfo("Singlethreaded scheduler started");

        for (var system : neverUpdate) {
            system.initialize();
        }

        for (var system : update) {
            system.initialize();
        }

        new Thread(this::detachedUpdate, "Wave Single Threaded Scheduler").start();

    }

    private void detachedUpdate() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var graphicalWaveSystem = waveEngineRunning.getRenderingSystem();
        graphicalWaveSystem.initialize();

        long lastUpdateTime = System.currentTimeMillis();
        while (true) {

            waveEngineRunning.getComponentManager().update();

            long waitTimeForFrame = 1000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetFramerate();

            while ((System.currentTimeMillis() - lastUpdateTime) < waitTimeForFrame) {
                if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                    Thread.onSpinWait();
                }
            }

            //rendering
            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            //updatable
            for (var system : update) { //todo delta by system?
                system.updateIteration(delta);
            }

            waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta); //todo add failsafe
        }


    }


    @Override
    public void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        if (started) {
            throw new IllegalStateException("Cannot add systems after engine start");
        }
        switch (updatePolicy) {
            case UPDATE_BEFORE_FRAME:
                update.add(waveSystem);
                break;
            case UPDATE_PARALLEL:
                update.add(waveSystem);
                break;
            case NEVER:
                neverUpdate.add(waveSystem);
                break;
        }
    }
}
