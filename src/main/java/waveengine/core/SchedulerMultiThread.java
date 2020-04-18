package waveengine.core;

import waveengine.ecs.system.WaveSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SchedulerMultiThread implements SchedulerImplementation {

    public SchedulerMultiThread(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        graphicalThread = new Thread(this::graphicUpdateLoop);
        executorServiceForParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs());
        executorServiceForAfterGraphicalParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForAfterGraphicsJobs());

        Logger.getLogger().logInfo(
                "Multi threaded scheduler created, with settings: numberOfThreadsForParallelJobs:" +
                        waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs() +
                        ", numberOfThreadsForAfterGraphicsJobs:" +
                        waveEngineRunning.getWaveEngineParameters().numberOfThreadsForAfterGraphicsJobs()
        );
    }

    private final WaveEngineRunning waveEngineRunning;
    private final Thread graphicalThread;
    private final ExecutorService executorServiceForParallelJobs;
    private final ExecutorService executorServiceForAfterGraphicalParallelJobs;

    private final Semaphore allowWorkSemaphore = new Semaphore(0);
    private final Semaphore workCompleteSemaphore = new Semaphore(0);

    @Override
    public void start() {
        Logger.getLogger().logInfo("Multithreaded scheduler started");
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

                boolean workingSuccessfully = true;

                while (workingSuccessfully) {
                    allowWorkSemaphore.acquireUninterruptibly();
                    double delta = (System.currentTimeMillis() - lastUpdateTime)/1000.0;
                    lastUpdateTime = System.currentTimeMillis();
                    try {
                        system.updateIteration(delta);
                    } catch (Exception e) {
                        Logger.getLogger().logError("Encountered exception with system: " + system.getName() + ". Exception: " + e.getMessage());
                        e.printStackTrace();
                        workingSuccessfully = false;
                        waveEngineRunning.getNotifyingService().asyncNotifyListeners(WaveEngineSystemEvents.EXCEPTION_WITH_SYSTEM, system.getName() + " caused exception.");
                    }
                    workCompleteSemaphore.release();
                }
            });
        }
    }

    private final List<WaveSystem> updateAfterFrame = new ArrayList<>();
    private final List<WaveSystem> updateParallel = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();

    @Override
    public void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
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


    private void graphicUpdateLoop() {
        var graphicalWaveSystem = waveEngineRunning.getRenderingSystem();

        graphicalWaveSystem.initialize();

        long lastUpdateTime = System.currentTimeMillis();

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {

            long waitTimeForFrame = 1000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetFramerate();

            while ((System.currentTimeMillis() - lastUpdateTime) < waitTimeForFrame) {
                if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                    Thread.onSpinWait();
                }
            }

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta); //todo add failsafe


            int workingSystems = updateAfterFrame.size();
            allowWorkSemaphore.release(workingSystems);
            workCompleteSemaphore.acquireUninterruptibly(workingSystems);
        }

    }

    private void updateParallelLoop(WaveSystem waveSystem) {
        waveSystem.initialize();

        long lastUpdateTime = System.currentTimeMillis();

        boolean workingSuccessfully = true;

        while (workingSuccessfully) {

            long waitTimeForFrame = 1000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetUPS();

            while ((System.currentTimeMillis() - lastUpdateTime) < waitTimeForFrame) {
                if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                    Thread.onSpinWait();
                }
            }

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            try {
                waveSystem.updateIteration(delta);
            } catch (Exception e) { //todo no longer possible
                Logger.getLogger().logError("Encountered exception with system: " + waveSystem.getName() + ". Exception: " + e.getMessage());
                e.printStackTrace();
                workingSuccessfully = false;
                waveEngineRunning.getNotifyingService().asyncNotifyListeners(WaveEngineSystemEvents.EXCEPTION_WITH_SYSTEM, waveSystem.getName() + " caused exception.");
            }
        }

    }
}
