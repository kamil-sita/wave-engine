package waveengine.ecs.component;


import waveengine.Discriminator;

public interface TableGroup {
    <T> Table<T> getTable(Discriminator table, Class<T> classOfT);

    default <T> Table<T> getTable(Class<T> classOfT) {
     return getTable(getComponentManager().getDiscriminatorForClass(classOfT), classOfT);
    }

    ComponentManager getComponentManager();

    interface WorkOnTable<T> {
        void doSthOnObject(Integer integer, T object);
    }

    interface Table<T> {
        void iterate(WorkOnTable<T> workOnTable);
        void iterateReverse(WorkOnTable<T> workOnTable);
        T get(Integer index);
    }

}
