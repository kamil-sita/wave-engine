package waveengine.core;

import waveengine.ecs.system.WaveSystem;
import waveengine.exception.ShutdownException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerMultiThread implements SchedulerImplementation {

    private AtomicInteger liveSystemCount = new AtomicInteger();

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
            try {
                while (waveEngineRunning.isRunning()) {
                    waveEngineRunning.getComponentManager().update();
                    allowWorkBetweenUpdates.release(systemCountForSemaphore);
                    workFinishedBetweenUpdates.acquire(systemCountForSemaphore);
                }
            } catch (InterruptedException e) {

            }
            Logger.getLogger().logInfo("Synchronized Multi Threaded Scheduler shut down");
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
        liveSystemCount.incrementAndGet();

        for (var system : neverUpdate) {
            system.initialize();
        }

        for (var system : updateParallel) {
            executorServiceForParallelJobs.submit(() -> {
                updateParallelLoop(system);
            });
            systemCountForSemaphore++;
            liveSystemCount.incrementAndGet();
        }

        for (var system : updateBeforeFrame) {
            executorServiceForBeforeGraphicalParallelJobs.submit(() -> {
                try {
                    system.initialize();

                    long lastUpdateTime = System.nanoTime();


                    //Parallel before frame completion is provided by semaphores.
                    while (waveEngineRunning.isRunning()) {
                        boolean acquired = false;
                        while (waveEngineRunning.isRunning() && !acquired) {
                            acquired = allowWorkBeforeFrame.tryAcquire(100, TimeUnit.MILLISECONDS);
                        }
                        if (!acquired) {
                            break;
                        }
                        double delta = (System.nanoTime() - lastUpdateTime)/1_000_000_000.0;
                        lastUpdateTime = System.nanoTime();
                        system.updateIteration(delta);
                        allowWorkAfterFrame.release();
                    }
                } catch (ShutdownException exception) {

                } catch (InterruptedException e) {

                }
                Logger.getLogger().logInfo(system.getFullName() + " shut down");
                liveSystemCount.decrementAndGet();
            });
            liveSystemCount.incrementAndGet();
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

    @Override
    public int getAliveSystemCount() {
        return liveSystemCount.get();
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

        try {
            while (waveEngineRunning.isRunning()) {

                long waitTimeForFrame = 1_000_000_000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetFramerate();

                while ((System.nanoTime() - lastUpdateTime) < waitTimeForFrame) {
                    if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                        Thread.onSpinWait();
                    }
                }
                boolean acquired = false;

                while (waveEngineRunning.isRunning() && !acquired) {
                    acquired = allowWorkBetweenUpdates.tryAcquire(100, TimeUnit.MILLISECONDS);
                }
                if (!acquired) {
                    break;
                }

                double delta = (System.nanoTime() - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = System.nanoTime();

                int workingSystems = updateBeforeFrame.size();
                allowWorkBeforeFrame.release(workingSystems);
                if (!waveEngineRunning.isRunning()) {
                    break;
                }

                acquired = false;

                while (waveEngineRunning.isRunning() && !acquired) {
                    acquired = allowWorkAfterFrame.tryAcquire(workingSystems, 100, TimeUnit.MILLISECONDS);
                }

                if (!acquired) {
                    break;
                }


                waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta);

                workFinishedBetweenUpdates.release();
            }
        } catch (ShutdownException shutdownException) {

        } catch (InterruptedException interruptedException) {

        }
        Logger.getLogger().logInfo(graphicalWaveSystem.getFullName() + " shut down");
        waveEngineRunning.getGuiImplementation().shutdown();
        liveSystemCount.decrementAndGet();
    }

    private void updateParallelLoop(WaveSystem waveSystem) {
        waveSystem.initialize();

        long lastUpdateTime = System.currentTimeMillis();


        try {
            while (waveEngineRunning.isRunning()) {

                long waitTimeForFrame = 1_000_000_000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetUPS();

                while ((System.nanoTime() - lastUpdateTime) < waitTimeForFrame) {
                    if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                        Thread.onSpinWait();
                    }
                }
                if (!waveEngineRunning.isRunning()) {
                    break;
                }

                boolean acquired = false;

                while (waveEngineRunning.isRunning() && !acquired) {
                    acquired = allowWorkBetweenUpdates.tryAcquire(100, TimeUnit.MILLISECONDS);
                }

                if (!acquired) {
                    break;
                }

                double delta = (System.nanoTime() - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = System.nanoTime();

                waveSystem.updateIteration(delta);

                workFinishedBetweenUpdates.release();
            }
        } catch (ShutdownException shutdownException) {

        } catch (InterruptedException e) {

        }
        Logger.getLogger().logInfo(waveSystem.getFullName() + " shut down");
        liveSystemCount.decrementAndGet();


    }
}
