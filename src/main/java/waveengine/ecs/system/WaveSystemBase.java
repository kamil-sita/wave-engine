package waveengine.ecs.system;

import waveengine.Discriminator;
import waveengine.core.WaveEngineRunning;
import waveengine.ecs.component.ManagedTableGroup;
import waveengine.ecs.component.TableGroup;
import waveengine.guiimplementation.Interactions;

import java.util.ArrayList;
import java.util.List;

public abstract class WaveSystemBase {
    private WaveEngineRunning waveEngineRunning;
    private List<ManagedTableGroup> resourcesHeld = new ArrayList<>();

    protected WaveEngineRunning getWaveEngineRunning() {
        return waveEngineRunning;
    }

    public WaveSystemBase setWaveEngineRunning(WaveEngineRunning waveEngineRunning) {
        if (waveEngineRunning == null) return this;
        this.waveEngineRunning = waveEngineRunning;
        return this;
    }

    protected ManagedTableGroup getTablesFor(Discriminator... components) {
        var comp = getWaveEngineRunning().getComponentManager().getTables(components);
        resourcesHeld.add(comp);
        return comp;
    }

    protected TableGroup.Table getTableFor(Discriminator component) {
        var comp = getTablesFor(component);
        return comp.getTable(component);
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
