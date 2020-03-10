package waveengine.ecs.component;

import waveengine.Discriminator;


/**
 * Managed ComponentContainer, that automatically releases resources upon using close() method (or using try-with-resources)
 * struct.
 */
public class ManagedTableGroup implements TableGroup, AutoCloseable {

    private TableGroup tableGroup;
    private ComponentManager.RemoveLockInterface removeLockInterface;
    private boolean closed = false;

    public ManagedTableGroup(TableGroup tableGroup, ComponentManager.RemoveLockInterface removeLockInterface) {
        this.tableGroup = tableGroup;
        this.removeLockInterface = removeLockInterface;
    }

    @Override
    public Table getTable(Discriminator table) {
        return tableGroup.getTable(table);
    }

    @Override
    public void close() {
        if (!closed) {
            removeLockInterface.remove();
            closed = true;
        }
    }
}
