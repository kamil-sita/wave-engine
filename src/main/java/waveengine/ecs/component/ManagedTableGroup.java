package waveengine.ecs.component;

import waveengine.Discriminator;


/**
 * Managed ComponentContainer, that automatically releases resources upon using close() method (or using try-with-resources)
 * struct.
 */
public class ManagedTableGroup implements TableGroup, AutoCloseable {

    private TableGroup tableGroup;
    private ComponentManager.RemoveLockInterface removeLockInterface;
    private ComponentManager componentManager;
    private boolean closed = false;

    public ManagedTableGroup(TableGroup tableGroup, ComponentManager componentManager, ComponentManager.RemoveLockInterface removeLockInterface) {
        this.tableGroup = tableGroup;
        this.removeLockInterface = removeLockInterface;
        this.componentManager = componentManager;
    }

    @Override
    public <T> Table<T> getTable(Discriminator table, Class<T> classOfT) {
        return tableGroup.getTable(table, classOfT);
    }

    @Override
    public ComponentManager getComponentManager() {
        return componentManager;
    }

    @Override
    public void close() {
        if (!closed) {
            removeLockInterface.remove();
            closed = true;
        }
    }
}
