package waveengine.ecs.system;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;
import waveengine.ecs.component.ManagedTableGroup;
import waveengine.ecs.component.TableGroup;
import waveengine.guiimplementation.Interactions;

import java.util.ArrayList;
import java.util.List;

public abstract class WaveSystemBase {
    private WaveEngineRunning waveEngineRunning;
    private List<ManagedTableGroup> resourcesHeld = new ArrayList<>();
    private String name;
    private String creator;
    private int iterationAcquiredResources = 0;
    private boolean iterationAcquiredResourcesWarningShown = false;

    public final String getName() {
        return name;
    }

    public String getCreator() {
        return "";
    }

    public final String getFullName() {
        return getName() + " [provider: " + getCreator() + "]";
    }

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public WaveSystemBase setName(String name) {
        this.name = name;
        return this;
    }

    protected WaveEngineRunning getWaveEngineRunning() {
        return waveEngineRunning;
    }

    public WaveSystemBase setWaveEngineRunning(WaveEngineRunning waveEngineRunning) {
        if (waveEngineRunning == null) return this;
        this.waveEngineRunning = waveEngineRunning;
        return this;
    }

    protected ManagedTableGroup getTablesFor(Discriminator... components) {
        iterationAcquiredResources++;
        if (iterationAcquiredResources > 1) {
            if (!iterationAcquiredResourcesWarningShown) {
                Logger.getLogger().logWarning(getFullName() + " acquires tables in multiple calls without freeing them. This might lead to data-races.");
                iterationAcquiredResourcesWarningShown = true;
            }
        }


        var comp = getWaveEngineRunning().getComponentManager().getTables(getName(), components);
        if (comp == null) {
            return null;
        }
        resourcesHeld.add(comp);
        return comp;
    }

    protected ManagedTableGroup getTablesFor(Class<?>... someClass) {
        var discs = getWaveEngineRunning().getComponentManager().getDiscriminatorForClass(someClass);
        return getTablesFor(discs);
    }

    protected <T> TableGroup.Table<T> getTableFor(Discriminator component, Class<T> classOfT) {
        var comp = getTablesFor(component);
        return comp.getTable(component, classOfT);
    }

    protected <T> TableGroup.Table<T> getTableFor(Class<T> classOfT) {
        var comp = getWaveEngineRunning().getComponentManager().getDiscriminatorForClass(classOfT);
        return getTableFor(comp, classOfT);
    }

    /**
     * Method that can be used to free resources acquired through getTablesFor method,
     * before finishing the current iteration - useful if you get resources in multiple calls, or no longer need them
     * and still need to do some work.
     */
    protected void freeComponents() {
        if (iterationAcquiredResources == 0) {
            return;
        }

        iterationAcquiredResources = 0;
        for (var comp : resourcesHeld) {
            comp.close();
        }
        resourcesHeld.clear();
    }

    protected WaveSystem getSystem(Discriminator systemDiscriminator) {
        return getWaveEngineRunning().getSystem(systemDiscriminator);
    }

    protected <T> T getSystem(Discriminator systemDiscriminator, Class<T> classOfT) {
        return (T) getWaveEngineRunning().getSystem(systemDiscriminator);
    }

    protected <T> T getSystem(Class<T> classOfT) {
        return (T) getWaveEngineRunning().getSystem(classOfT);
    }

    protected Interactions getInteractions() {
        return getWaveEngineRunning().getInteractions();
    }
}
