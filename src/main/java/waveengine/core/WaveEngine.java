package waveengine.core;


import waveengine.Discriminator;
import waveengine.WaveEngineParameters;
import waveengine.WaveEngineRuntimeSettings;
import waveengine.ecs.component.ComponentManager;
import waveengine.ecs.entity.EntityBuilder;
import waveengine.ecs.system.RenderingSystem;
import waveengine.services.NotifyingService;
import waveengine.ecs.system.WaveSystem;

public final class WaveEngine {

    private final WaveEngineParameters waveEngineParameters;
    private final WaveEngineRuntimeSettings waveEngineRuntimeSettings = new WaveEngineRuntimeSettings();
    private final WaveEngineRunning waveEngineRunning;
    private final EntityBuilder entityBuilder;

    public static WaveEngine newInstance(WaveEngineParameters parameters,  RenderingSystem renderingSystem) {
        Logger.getLogger().logInfo("Creating new WaveEngine instance");
        return new WaveEngine(parameters, renderingSystem);
    }


    private WaveEngine(WaveEngineParameters parameters, RenderingSystem renderingSystem) {
        this.waveEngineParameters = parameters;
        renderingSystem.setName("RENDERING SYSTEM");
        waveEngineRunning = new WaveEngineRunning(this);
        entityBuilder = new EntityBuilder(waveEngineRunning.getComponentManager());
        renderingSystem.setWaveEngineRunning(waveEngineRunning);
        waveEngineRunning.setRenderingSystem(renderingSystem);
    }


    /**
     * Starts WaveEngine.
     */
    public void launch() {
        Logger.getLogger().logInfo("Launching WaveEngine");
        Logger.getLogger().logInfo("Setting properties");
        System.setProperty("sun.java2d.translaccel", "True");
        System.setProperty("sun.java2d.opengl", "True");
        Logger.getLogger().logInfo("Configuring self services");
        configureSelfServices();
        Logger.getLogger().logInfo("Launching");
        waveEngineRunning.launch();
    }

    public void addSystem(Discriminator systemDiscriminator, UpdatePolicy updatePolicy, WaveSystem system) {
        if (!waveEngineRunning.getSystems().containsKey(systemDiscriminator)) {
            if (!system.hasName()) {
                system.setName(systemDiscriminator.toString());
            }
            system.setWaveEngineRunning(waveEngineRunning);
            waveEngineRunning.getSystems().put(systemDiscriminator, system);
            waveEngineRunning.getScheduler().addSystem(system, updatePolicy);
            Logger.getLogger().logInfo("Added system: " + system.getName());
        } else {
            Logger.getLogger().logError("System with discriminator: " + systemDiscriminator.toString() + " already exists");
        }
    }

    public void addListener(Discriminator discriminator, NotifyingService.Notifier notifier) {
        waveEngineRunning.getNotifyingService().addListener(discriminator, notifier);
    }

    WaveEngineRuntimeSettings getWaveEngineRuntimeSettings() {
        return waveEngineRuntimeSettings;
    }

    WaveEngineParameters getWaveEngineParameters() {
        return waveEngineParameters;
    }

    public ComponentManager getComponentManager() {
        return waveEngineRunning.getComponentManager();
    }

    public void setInitialStage(Discriminator stage) {
        waveEngineRunning.setCurrentStage(stage);
    }

    @Deprecated
    public WaveEngineRunning getWaveEngineRunning() {
        return waveEngineRunning;
    }

    private void configureSelfServices() {
        //checking memory service
        if (waveEngineParameters.useMemoryFreeingService()) {
            addSystem(
                    WaveCoreSystemDiscriminator.MEMORY_CHECKER_SYSTEM,
                    UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
                        double timeSinceFreeAttempt = 0;

                        @Override
                        public void update(double deltaTime) {
                            timeSinceFreeAttempt += deltaTime;
                            if (lessMemoryThanNeeded()) {
                                if (timeSinceFreeAttempt > 5) {
                                    waveEngineRunning.getNotifyingService().asyncNotifyListeners(
                                            WaveEngineSystemEvents.LOW_ON_MEMORY,
                                            "PROGRAM HAS ONLY FREE " + Runtime.getRuntime().freeMemory()/(1024*1024) + " MEMORY OUT OF MAX " + Runtime.getRuntime().maxMemory()/(1024*1024)
                                    );
                                    timeSinceFreeAttempt = 0;
                                }
                            }

                        }

                        private boolean lessMemoryThanNeeded() {
                            return (1.0 * Runtime.getRuntime().freeMemory())/(Runtime.getRuntime().maxMemory()) < 0.99;
                        }
                    }
            );
        }

        //
    }

    public EntityBuilder getEntityBuilder() {
        return entityBuilder;
    }

}
