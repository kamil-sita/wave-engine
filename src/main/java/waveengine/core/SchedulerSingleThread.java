package waveengine.core;

import waveengine.ecs.system.WaveSystem;

import java.util.ArrayList;
import java.util.List;

public class SchedulerSingleThread implements SchedulerImplementation {

    private final WaveEngineRunning waveEngineRunning;
    private final List<WaveSystem> update = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();

    public SchedulerSingleThread(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        Logger.getLogger().logInfo("Single threaded scheduler created");
    }

    @Override
    public void start() {
        Logger.getLogger().logInfo("Singlethreaded scheduler started");

        for (var system : neverUpdate) {
            system.initialize();
        }

        for (var system : update) {
            system.initialize();
        }

        new Thread(this::detachedUpdate).start();

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

            long waitTimeForFrame = 1000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetFramerate();

            while ((System.currentTimeMillis() - lastUpdateTime) < waitTimeForFrame) {
                if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                    Thread.onSpinWait();
                }
            }

            //rendering
            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta); //todo add failsafe

            //updatable
            for (var system : update) { //todo delta by system?
                system.updateIteration(delta);
            }
        }


    }


    @Override
    public void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        switch (updatePolicy) {
            case UPDATE_AFTER_DRAWING_FRAME_PARALLEL:
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
