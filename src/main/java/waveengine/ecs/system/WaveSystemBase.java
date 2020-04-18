package waveengine.ecs.system;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;
import waveengine.ecs.component.ManagedTableGroup;
import waveengine.ecs.component.Semaphoring;
import waveengine.ecs.component.TableGroup;
import waveengine.guiimplementation.Interactions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class WaveSystemBase {
    private WaveEngineRunning waveEngineRunning;
    private List<ManagedTableGroup> resourcesHeld = new ArrayList<>();
    private String name;

    public String getName() {
        if (name == null || name.isEmpty()) {
            name = this.getClass().getName() + "@" + this.hashCode();
        }
        return name;
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

    protected ManagedTableGroup getTablesFor(Discriminator... components) throws Semaphoring.TableNotOwnedException {
        var comp = getWaveEngineRunning().getComponentManager().getTables(getName(), components);
        if (comp == null) {
            return null;
        }
        resourcesHeld.add(comp);
        return comp;
    }

    protected <T> TableGroup.Table<T> getTableFor(Discriminator component, Class<T> classOfT) throws Semaphoring.TableNotOwnedException {
        var comp = getTablesFor(component);
        return comp.getTable(component, classOfT);
    }

    /**
     * Method that can be used to free resources acquired through getTablesFor method,
     * before finishing the current iteration - useful if you
     */
    protected void freeComponents() {
        for (var comp : resourcesHeld) {
            comp.close();
        }
        resourcesHeld.clear();
    }

    protected WaveSystem getSystem(Discriminator systemDiscriminator) {
        return getWaveEngineRunning().getSystem(systemDiscriminator);
    }

    protected Interactions getInteractions() {
        return getWaveEngineRunning().getInteractions();
    }
}
