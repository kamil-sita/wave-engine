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
        graphicalThread = new Thread(this::graphicUpdateLoop, "Wave Multi Threaded Scheduler Graphical Thread");
        executorServiceForParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs());
        executorServiceForBeforeGraphicalParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForAfterGraphicsJobs());

        Logger.getLogger().logInfo(
                "Multi threaded scheduler created, with settings: numberOfThreadsForParallelJobs:" +
                        waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs() +
                        ", numberOfThreadsForAfterGraphicsJobs:" +
                        waveEngineRunning.getWaveEngineParameters().numberOfThreadsForAfterGraphicsJobs()
        );

        synchronizedUpdaterThread = new Thread(() -> {
            while (true) {
                waveEngineRunning.getComponentManager().update();
                allowWorkBetweenUpdates.release(systemCountForSemaphore);
                workFinishedBetweenUpdates.acquireUninterruptibly(systemCountForSemaphore);
            }
        }, "Wave Synchronized Multi Threaded Scheduler Update Thread");
    }


    private final WaveEngineRunning waveEngineRunning;
    private final Thread graphicalThread;
    private final ExecutorService executorServiceForParallelJobs;
    private final ExecutorService executorServiceForBeforeGraphicalParallelJobs;
    private final Thread synchronizedUpdaterThread;

    private final Semaphore allowWorkBeforeFrame = new Semaphore(0);
    private final Semaphore allowWorkAfterFrame = new Semaphore(0);

    private int systemCountForSemaphore;
    private boolean started = false;
    private final Semaphore allowWorkBetweenUpdates = new Semaphore(0);
    private final Semaphore workFinishedBetweenUpdates = new Semaphore(0);

    @Override
    public void start() {
        started = true;
        Logger.getLogger().logInfo("Multithreaded scheduler started");
        graphicalThread.start();

        systemCountForSemaphore = 1;

        for (var system : neverUpdate) {
            system.initialize();
        }

        for (var system : updateParallel) {
            executorServiceForParallelJobs.submit(() -> {
                updateParallelLoop(system);
            });
            systemCountForSemaphore++;
        }

        for (var system : updateBeforeFrame) {
            executorServiceForBeforeGraphicalParallelJobs.submit(() -> {
                system.initialize();

                long lastUpdateTime = System.currentTimeMillis();

                boolean workingSuccessfully = true;

                /*
                Parallel before frame completion is provided by semaphores.
                 */
                while (workingSuccessfully) {
                    allowWorkBeforeFrame.acquireUninterruptibly();
                    double delta = (System.currentTimeMillis() - lastUpdateTime)/1000.0;
                    lastUpdateTime = System.currentTimeMillis();
                    try {
                        system.updateIteration(delta);
                    } catch (Exception e) {
                        Logger.getLogger().logError("Encountered exception with system: " + system.getName() + ". Exception: " + e.getMessage());
                        e.printStackTrace();
                        workingSuccessfully = false;
                        waveEngineRunning.getNotifyingService().notifyListeners(WaveEngineSystemEvents.EXCEPTION_WITH_SYSTEM, system.getName() + " caused exception.");
                    }
                    allowWorkAfterFrame.release();
                }
            });
        }

        synchronizedUpdaterThread.start();
    }

    private final List<WaveSystem> updateBeforeFrame = new ArrayList<>();
    private final List<WaveSystem> updateParallel = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();

    @Override
    public void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        if (started) {
            throw new IllegalStateException("Cannot add systems after engine start");
        }
        switch (updatePolicy) {
            case UPDATE_BEFORE_FRAME:
                updateBeforeFrame.add(waveSystem);
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
            allowWorkBetweenUpdates.acquireUninterruptibly();

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            int workingSystems = updateBeforeFrame.size();
            allowWorkBeforeFrame.release(workingSystems);
            allowWorkAfterFrame.acquireUninterruptibly(workingSystems);

            waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta); //todo add failsafe

            workFinishedBetweenUpdates.release();
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
            allowWorkBetweenUpdates.acquireUninterruptibly();

            double delta = (System.currentTimeMillis() - lastUpdateTime) / 1000.0;
            lastUpdateTime = System.currentTimeMillis();

            try {
                waveSystem.updateIteration(delta);
            } catch (Exception e) { //todo no longer possible
                Logger.getLogger().logError("Encountered exception with system: " + waveSystem.getName() + ". Exception: " + e.getMessage());
                e.printStackTrace();
                workingSuccessfully = false;
                waveEngineRunning.getNotifyingService().notifyListeners(WaveEngineSystemEvents.EXCEPTION_WITH_SYSTEM, waveSystem.getName() + " caused exception.");
            }

            workFinishedBetweenUpdates.release();
        }

    }
}
