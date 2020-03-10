package waveengine.ecs.component;


import waveengine.Discriminator;

public interface TableGroup {
    Table getTable(Discriminator table);

    interface WorkOnTable {
        void doSthOnObject(Integer integer, Object object);
    }

    interface Table {
        void iterate(boolean ordered, WorkOnTable workOnTable);
        Object get(Integer index);
    }
}
