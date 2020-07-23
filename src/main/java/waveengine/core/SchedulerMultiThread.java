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
    private volatile boolean awaitsUpdate = false;

    public SchedulerMultiThread(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        graphicalThread = new Thread(this::graphicUpdateLoop, "Wave Multi Threaded Scheduler Graphical Thread");
        executorServiceForParallelJobs = Executors.newFixedThreadPool(waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs());

        Logger.getLogger().logInfo(
                "Multi threaded scheduler created, with settings: numberOfThreadsForParallelJobs:" +
                        waveEngineRunning.getWaveEngineParameters().numberOfThreadsForParallelJobs()
        );

        synchronizedUpdaterThread = new Thread(() -> {
            try {
                var componentManager = waveEngineRunning.getComponentManager();
                while (waveEngineRunning.isRunning()) {
                    if (componentManager.isUpdateNeeded()) {
                        awaitsUpdate = true;
                        for (int i = 0; i < systemCountForSemaphore; i++) {
                            readyForUpdate.acquire();
                        }
                        //workFinishedBetweenUpdates.acquire(systemCountForSemaphore);
                        componentManager.update();
                        awaitsUpdate = false;
                        updateFinished.release(systemCountForSemaphore);
                    }
                    Thread.onSpinWait();
                }
            } catch (InterruptedException e) {

            }
            Logger.getLogger().logInfo("Synchronized Multi Threaded Scheduler shut down");
        }, "Wave Synchronized Multi Threaded Scheduler Update Thread");
    }


    private final WaveEngineRunning waveEngineRunning;
    private final Thread graphicalThread;
    private final ExecutorService executorServiceForParallelJobs;
    private final Thread synchronizedUpdaterThread;

    private int systemCountForSemaphore;
    private boolean started = false;
    private final Semaphore updateFinished = new Semaphore(0);
    private final Semaphore readyForUpdate = new Semaphore(0);

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

        synchronizedUpdaterThread.start();
    }

    private final List<WaveSystem> updateParallel = new ArrayList<>();
    private final List<WaveSystem> neverUpdate = new ArrayList<>();

    @Override
    public void addSystem(WaveSystem waveSystem, UpdatePolicy updatePolicy) {
        if (started) {
            throw new IllegalStateException("Cannot add systems after engine start");
        }
        switch (updatePolicy) {
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
            System.out.println(graphicalWaveSystem.hashCode() + " is graphical");
            while (waveEngineRunning.isRunning()) {

                long waitTimeForFrame = 1_000_000_000 / waveEngineRunning.getWaveEngineRuntimeSettings().getTargetFramerate();

                while ((System.nanoTime() - lastUpdateTime) < waitTimeForFrame) {
                    if (waveEngineRunning.getWaveEngineParameters().useSystemWaitSpinOnWait()) {
                        Thread.onSpinWait();
                    }
                }
                boolean acquired = false;

                if (awaitsUpdate) {
                    readyForUpdate.release();
                    while (waveEngineRunning.isRunning() && !acquired) {
                        acquired = updateFinished.tryAcquire(100, TimeUnit.MILLISECONDS);
                    }
                }

                if (!waveEngineRunning.isRunning()) {
                    break;
                }

                double delta = (System.nanoTime() - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = System.nanoTime();

                waveEngineRunning.getGuiImplementation().updateRenderingSystem(waveEngineRunning, delta);
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
            System.out.println(waveSystem.hashCode() + " is parallel");
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


                if (awaitsUpdate) {
                    readyForUpdate.release();
                    while (waveEngineRunning.isRunning() && !acquired) {
                        acquired = updateFinished.tryAcquire(100, TimeUnit.MILLISECONDS);
                    }
                }
                if (!waveEngineRunning.isRunning()) {
                    break;
                }

                double delta = (System.nanoTime() - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = System.nanoTime();

                waveSystem.updateIteration(delta);
            }
        } catch (ShutdownException shutdownException) {

        } catch (InterruptedException e) {

        }
        Logger.getLogger().logInfo(waveSystem.getFullName() + " shut down");
        liveSystemCount.decrementAndGet();


    }
}
