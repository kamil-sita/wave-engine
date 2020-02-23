package waveengine.core;

import waveengine.ecs.system.WaveSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Scheduler {

    private final WaveEngineRunning waveEngineRunning;

    public Scheduler(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        graphicalThread = new Thread(this::graphicUpdateLoop);
        executorServiceForParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs());
        executorServiceForAfterGraphicalParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForAfterGraphicsJobs());
    }

    private final List<WaveSystem> updateAfterFrame = new ArrayList<>();
    private final List<WaveSystem> updateParallel = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();

    void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        switch (updatePolicy) {
            case UPDATE_AFTER_DRAWING_FRAME_PARALLEL:
                updateAfterFrame.add(waveSystem);
                break;
            case UPDATE_PARALLEL:
                updateParallel.add(waveSystem);
                break;
            case NEVER:
                neverUpdate.add(waveSystem);
                break;
        }
    }

    private final Thread graphicalThread;
    private final ExecutorService executorServiceForParallelJobs;
    private final ExecutorService executorServiceForAfterGraphicalParallelJobs;

    private final Semaphore allowWorkSemaphore = new Semaphore(0);
    private final Semaphore workCompleteSemaphore = new Semaphore(0);

    void start() {
        graphicalThread.start();

        for (var system : neverUpdate) {
            system.initialize();;
        }

        for (var system : updateParallel) {
            executorServiceForParallelJobs.submit(() -> {
                updateParallelLoop(system);
            });
        }

        for (var system : updateAfterFrame) {
            executorServiceForAfterGraphicalParallelJobs.submit(() -> {
                system.initialize();

                long lastUpdateTime = System.currentTimeMillis();

                while (true) {
                    allowWorkSemaphore.acquireUninterruptibly();
                    double delta = (System.currentTimeMillis() - lastUpdateTime)/1000.0;
                    lastUpdateTime = System.currentTimeMillis();
                    system.updateIteration(delta);
                    workCompleteSemaphore.release();
                }
            });
        }
    }

    private void graphicUpdateLoop() {
        waveEngineRunning.getRenderingSystem().initialize();

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

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta);

            int workingSystems = updateAfterFrame.size();
            allowWorkSemaphore.release(workingSystems);
            workCompleteSemaphore.acquireUninterruptibly(workingSystems);
        }

    }

    private void updateParallelLoop(WaveSystem waveSystem) {
        waveSystem.initialize();

        long lastUpdateTime = System.currentTimeMillis();

        while (true) {

            long waitTimeForFrame = 1000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetUPS();

            while ((System.currentTimeMillis() - lastUpdateTime) < waitTimeForFrame) {
                if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                    Thread.onSpinWait();
                }
            }

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            waveSystem.updateIteration(delta);
        }

    }
}
