package waveengine.ecs.component;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.entity.Entity;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ComponentManager {

    private final WaveEngineRunning waveEngineRunning;

    private Discriminator currentStage; //active stage until it might change after update() call
    private Discriminator nextStage; //next stage that will be changed during update() call

    //todo both entities and componentsPerEntity should contain the same sets. Can they be merged?
    private final Set<Entity> entities = new HashSet<>(5000); //contains all entities in engine
    private final Map<Entity, EntityComponents> componentsPerEntity = Collections.synchronizedMap(new HashMap<>(5000)); //contains all components in system, divided by entity and system

    private final Set<Entity> activeEntities = new HashSet<>();

    private final Set<Entity> addingSet = Collections.synchronizedSet(new HashSet<>()); //set with all entities that will be added in next iteration
    private final Set<Entity> removingSet = new HashSet<>(); //set with all entities that will be removed in next iteration

    private Map<Discriminator, Map<Integer, Object>> cacheForThisStage = new HashMap<>(); //todo optimize? Or too hard?

    public ComponentManager(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    private AtomicInteger resourcesHeld = new AtomicInteger(0);

    public void update() {
        //todo make sure no one has any resource
        if (resourcesHeld.get() != 0) {
            throw new IllegalStateException("No resources should be held");
        }

        boolean changed = false;

        if (!addingSet.isEmpty() || !removingSet.isEmpty()) { //todo optimize?
            changed = true;
        }

        //adding entities
        for (Entity entity : addingSet) {
            if (entity.isActive(currentStage)) {
                activeEntities.add(entity);
            }
        }
        entities.addAll(addingSet);

        //removing entities
        activeEntities.removeAll(removingSet);

        for (Entity entity : removingSet) {
            componentsPerEntity.remove(entity);
        }
        entities.removeAll(removingSet);


        addingSet.clear();
        removingSet.clear();

        //change stage
        if (this.nextStage != null) {
            changed = true;
            this.currentStage = nextStage;
            nextStage = null;

            rebuildActiveEntities();

            waveEngineRunning.getNotifyingService().asyncNotifyListeners(WaveEngineSystemEvents.STAGE_CHANGED, currentStage);
        }

        if (changed) {
            cacheForThisStage.clear();
        }
    }

    private void rebuildActiveEntities() {
        activeEntities.clear();

        for (Entity entity : entities) {
            if (entity.isActive(currentStage)) {
                activeEntities.add(entity);
            }
        }

    }

    public ComponentManager setTargetStage(Discriminator nextStage) {
        this.nextStage = nextStage;
        return this;
    }

    public void addEntityToComponent(Entity entity, Class<?> clazz, Object component) {
        addEntityToComponent(entity, getDiscriminatorForClass(clazz), component);
    }

    public void addEntityToComponent(Entity entity, Discriminator discriminator, Object component) {
        //todo thread safe
        addingSet.add(entity);
        if (componentsPerEntity.containsKey(entity)) {
            componentsPerEntity.get(entity).addComponent(discriminator, component);
        } else {
            EntityComponents entityComponents = new EntityComponents();
            entityComponents.addComponent(discriminator, component);
            componentsPerEntity.put(entity, entityComponents);
        }
    }

    public void removeEntity(Entity entity) {
        removingSet.add(entity);
    }

    private Map<Discriminator, Semaphore> lockedTables = Collections.synchronizedMap(new HashMap<>()); //todo also probably too expensive (timely) of solution, like in EntityComponents.java

    /**
     * Method that locks given tables, and returns combination of those tables as ManagedTableGroup object.
     * @param selectedTables tables that are required.
     * @return ManagedTableGroup object with required tables.
     */
    public ManagedTableGroup getTables(String owner, Discriminator... selectedTables) {
        //todo better threading
        lockObtain(owner, selectedTables);
        var tables = getTables(owner, Arrays.asList(selectedTables));
        return new ManagedTableGroup(
                tables,
                this,
                () -> lockRelease(selectedTables));
    }

    private TableGroup getTables(String owner, List<Discriminator> asList) { //todo lock should be obtained, so no reason to fear about racing
        var as = new ComponentContainerImpl(this);

        for (var component : asList) {
            if (cacheForThisStage.getOrDefault(component, Map.of()).isEmpty()) {
                Map<Integer, Object> objects = new HashMap<>();
                for (Entity entity : activeEntities) {
                    EntityComponents components = componentsPerEntity.get(entity);
                    if (components.hasComponent(component)) {
                        objects.put(entity.getId(), components.getComponent(component));
                    }
                }
                cacheForThisStage.put(component, objects);
            }
            as.add(component, cacheForThisStage.get(component));
        }
        return as;
    }

    private Semaphore workaroundsemaphore = new Semaphore(1);

    private void lockObtain(String owner, Discriminator... selectedTables) {
        //todo optimizations
        List<Discriminator> sortedDiscriminators = new ArrayList<>(Arrays.asList(selectedTables));

        //
        sortedDiscriminators.sort(Comparator.comparingInt(Discriminator::hashCode));

        for (Discriminator discriminator : sortedDiscriminators) {
            workaroundsemaphore.acquireUninterruptibly();
            if (!lockedTables.containsKey(discriminator)) {
                lockedTables.put(discriminator, new Semaphore(1));
            }
            workaroundsemaphore.release();
            lockedTables.get(discriminator).acquireUninterruptibly();
            resourcesHeld.incrementAndGet();
        }

    }

    private void lockRelease(Discriminator... selectedTables) {
        for (Discriminator discriminator : selectedTables) {
            lockedTables.get(discriminator).release();
            resourcesHeld.decrementAndGet();
        }
    }


    private final Map<Class<?>, Discriminator> discriminatorForClass = Collections.synchronizedMap(new HashMap<>());

    public Discriminator[] getDiscriminatorForClass(Class<?>[] someClass) {
        Discriminator[] discriminators = new Discriminator[someClass.length];
        for (int i = 0; i < someClass.length; i++) {
            discriminators[i] = getDiscriminatorForClass(someClass[i]);
        }
        return discriminators;
    }

    public <T> Discriminator getDiscriminatorForClass(Class<T> classOfT, String suggestedName) {
        discriminatorForClass.computeIfAbsent(classOfT, new Function<Class<?>, Discriminator>() {
            @Override
            public Discriminator apply(Class<?> aClass) {
                if (suggestedName == null) {
                    Logger.getLogger().log("Generating Discriminator for class: " + classOfT + ", got: " + aClass.getName());
                    return Discriminator.fromString(aClass.getName());
                } else {
                    Logger.getLogger().log("Using suggested name of Discriminator for class: " + classOfT + ", got: " + suggestedName);
                    return Discriminator.fromString(suggestedName);
                }
            }
        });

        return discriminatorForClass.get(classOfT);
    }

    public <T> Discriminator getDiscriminatorForClass(Class<T> classOfT) {
        return getDiscriminatorForClass(classOfT, null);
    }

    /**
     * Used by handles provided by this ComponentManager to release resources (tables that are locked).
     */
    @FunctionalInterface
    public interface RemoveLockInterface {
        void remove();
    }

}
