package waveengine.core;

import waveengine.Discriminator;
import waveengine.WaveEngineParameters;
import waveengine.WaveEngineRuntimeSettings;
import waveengine.ecs.component.ComponentManager;
import waveengine.ecs.entity.EntityBuilder;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.exception.ShutdownException;
import waveengine.guiimplementation.GuiImplementation;
import waveengine.guiimplementation.Interactions;
import waveengine.guiimplementation.Renderer;
import waveengine.services.NotifyingService;

import java.util.HashMap;
import java.util.Map;

public class WaveEngineRunning {
    private WaveEngine waveEngine;
    private Map<Discriminator, WaveSystem> systems = new HashMap<>();
    private RenderingSystem renderingSystem;
    private final NotifyingService notifyingService = new NotifyingService(this);
    private Discriminator currentStage = DefaultStageDiscriminator.STAGE_DEFAULT;
    private Scheduler scheduler;
    private GuiImplementation guiImplementation = new GuiImplementation(this);
    private Renderer renderer = new Renderer(this);
    private ComponentManager facade;

    private boolean isRunning = true; //todo after scheduler starts


    public WaveEngineRunning(WaveEngine waveEngine) {
        this.waveEngine = waveEngine;
        facade = new ComponentManager(this);
    }

    Scheduler getScheduler() {
        if (scheduler == null) scheduler = new Scheduler(this);
        return scheduler;
    }

    public Interactions getInteractions() {
        return getGuiImplementation().getInteractions();
    }

    @Deprecated
    public WaveEngine getWaveEngine() {
        return waveEngine;
    }

    Map<Discriminator, WaveSystem> getSystems() {
        return systems;
    }

    public void setNextStage(Discriminator stage) {
        getComponentManager().setTargetStage(stage);
    }

    public Discriminator getCurrentStage() {
        return getComponentManager().getCurrentStage();
    }

    WaveEngineRunning setRenderingSystem(RenderingSystem renderingSystem) {
        this.renderingSystem = renderingSystem;
        return this;
    }

    public NotifyingService getNotifyingService() {
        return notifyingService;
    }


    public WaveSystem getSystem(Discriminator systemDiscriminator) {
        return systems.get(systemDiscriminator);
    }

    public WaveSystem getSystem(Class<?> classOfT) {
        return getSystem(getComponentManager().getDiscriminatorForClass(classOfT));
    }


    public WaveEngineRuntimeSettings getWaveEngineRuntimeSettings() {
        return waveEngine.getWaveEngineRuntimeSettings();
    }

    public WaveEngineParameters getWaveEngineParameters() {
        return waveEngine.getWaveEngineParameters();
    }

    GuiImplementation getGuiImplementation() {
        return guiImplementation;
    }

    void launch() {
        guiImplementation.initialize();
        scheduler.start();
    }

    public RenderingSystem getRenderingSystem() {
        return renderingSystem;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    WaveEngineRunning setRenderer(Renderer renderer) { //todo?? in runtime??
        this.renderer = renderer;
        return this;
    }

    public ComponentManager getComponentManager() {
        return facade;
    }

    public EntityBuilder getEntityBuilder() {
        return waveEngine.getEntityBuilder();
    }

    public void shutdown(String s, boolean throwsException) {
        if (!isRunning) {
            Logger.getLogger().logShutdown("Additional shutdown: " + s);
            if (throwsException) {
                throw new ShutdownException();
            }
            return;
        }
        Logger.getLogger().logShutdown(s);
        isRunning = false;

        new Thread(() -> {
            while (true) {
                Logger.getLogger().logInfo("Shutting down after all systems stop, " + getScheduler().getAliveSystemCount() + " are still alive");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int alive = getScheduler().getAliveSystemCount();
                if (alive == 0) {
                    Logger.getLogger().logInfo("All systems shut down");
                    System.exit(0); //todo find thread leaks
                }
            }
        }, "Shutdown thread").start();


        if (throwsException) {
            throw new ShutdownException();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
