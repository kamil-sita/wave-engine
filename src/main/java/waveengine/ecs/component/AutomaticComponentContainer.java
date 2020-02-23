package waveengine.ecs.component;

import waveengine.Discriminator;


public class AutomaticComponentContainer implements TableGroup, AutoCloseable {

    private TableGroup componentContainer;
    private ComponentManager.RemoveLockInterface removeLockInterface;
    private boolean closed = false;

    public AutomaticComponentContainer(TableGroup componentContainer, ComponentManager.RemoveLockInterface removeLockInterface) {
        this.componentContainer = componentContainer;
        this.removeLockInterface = removeLockInterface;
    }

    @Override
    public Table getTable(Discriminator component) {
        return componentContainer.getTable(component);
    }

    @Override
    public void close() {
        if (!closed) {
            removeLockInterface.remove();
            closed = true;
        }
    }
}
