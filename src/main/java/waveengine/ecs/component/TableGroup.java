package waveengine.ecs.component;


import waveengine.Discriminator;

public interface TableGroup {
    <T> Table<T> getTable(Discriminator table, Class<T> classOfT);

    interface WorkOnTable<T> {
        void doSthOnObject(Integer integer, T object);
    }

    interface Table<T> {
        void iterate(boolean ordered, WorkOnTable<T> workOnTable);
        void iterateReverse(WorkOnTable<T> workOnTable);
        T get(Integer index);
    }
}
