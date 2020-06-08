package waveengine.ecs.component;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.entity.Entity;
import waveengine.services.NotifyingService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ComponentManager {

    private Discriminator activeStage;

    private Map<Discriminator, HashMap<Integer, Object>> objectsPerComponent = new HashMap<>();
    private Map<Discriminator, HashMap<Integer, Object>> activeObjectsPerComponent = new HashMap<>();
    private Map<List<Discriminator>, TableGroup> activeObjectsPerComponents = new HashMap<>();
    private List<Entity> entityList = new ArrayList<>();
    private Set<Discriminator> components = new HashSet<>();
    private WaveEngineRunning waveEngineRunning;


    public ComponentManager(WaveEngineRunning waveEngineRunning) {
        waveEngineRunning.getNotifyingService().addListener(WaveEngineSystemEvents.STAGE_CHANGED, new NotifyingService.Notifier() {
            @Override
            public void notifyListener(Discriminator cause, Object message) {
                setActiveStage((Discriminator) message);
            }
        });
        semaphoring = new Semaphoring(waveEngineRunning);
        this.waveEngineRunning = waveEngineRunning;
    }

    private ComponentManager setActiveStage(Discriminator activeStage) {
        semaphoring.modificationLockObtain();
        invalidateCache();
        this.activeStage = activeStage;
        semaphoring.modificationLockRelease();
        return this;
    }

    private boolean needsToRebuildList = true;

    private Semaphoring semaphoring;

    private Map<Discriminator, Semaphore> discriminatorSemaphoreMap = new ConcurrentHashMap<>();

    /**
     * Method that locks given tables, and returns combination of those tables as ManagedTableGroup object.
     * @param selectedTables tables that are required.
     * @return ManagedTableGroup object with required tables.
     */
    public ManagedTableGroup getTables(String owner, Discriminator... selectedTables) throws Semaphoring.TableNotOwnedException {
        Semaphoring.TableNotOwnedException exception = null;
        exception = semaphoring.exclusiveLockObtain(selectedTables, owner);
        if (exception != null) {
            throw exception;
        }
        var tables = lazyGetTables(owner, Arrays.asList(selectedTables));
        return new ManagedTableGroup(
                tables,
                this,
                () -> semaphoring.exclusiveLockRelease(selectedTables, owner));
    }

    private synchronized TableGroup lazyGetTables(String owner, List<Discriminator> components) {
        semaphoring.modificationLockObtain();

        if (activeObjectsPerComponents.containsKey(components)) {
            var retVal = activeObjectsPerComponents.get(components);
            semaphoring.modificationLockRelease();
            return retVal;
        }

        if (needsToRebuildList) {
            rebuildActiveObjectsPerComponent();
            needsToRebuildList = false;
        }

        for (var component : components) {
            buildForSystem(component);
        }

        var as = new ComponentContainerImpl(this);
        for (var component : components) {
            as.add(component, activeObjectsPerComponent.get(component));
        }
        activeObjectsPerComponents.put(components, as);

        semaphoring.modificationLockRelease();
        return as;
    }

    private void rebuildActiveObjectsPerComponent() {
        activeObjectsPerComponent = new HashMap<>();
        for (var component : components) {
            HashMap<Integer, Object> activeObjectsForThisComponent = new HashMap<>();
            var objectsForThisComponent = objectsPerComponent.get(component);

            for (var entry : objectsForThisComponent.entrySet()) {
                var id = entry.getKey();

                if (entityList.get(id).isActive(activeStage)) {
                    activeObjectsForThisComponent.put(id, entry.getValue());
                }
            }
            activeObjectsPerComponent.put(component, activeObjectsForThisComponent);
        }
    }


    private void invalidateCacheIfSystemIsActive(Entity entity) {
        if (entity.isActive(activeStage)) {
            invalidateCache();
        }
    }

    private void invalidateCache() {
        activeObjectsPerComponent = new HashMap<>();
        activeObjectsPerComponents = new HashMap<>();
        needsToRebuildList = true;
    }

    private void buildForSystem(Discriminator discriminator) {
        if (activeObjectsPerComponent.containsKey(discriminator)) return;

        HashMap<Integer, Object> map = new HashMap<>();
        activeObjectsPerComponent.put(discriminator, map);
        var objectForSystem = objectsPerComponent.getOrDefault(discriminator, new HashMap<>());
        for (var object : objectForSystem.entrySet()) {
            int id = object.getKey();
            if (entityList.get(id).isActive(discriminator)) {
                map.put(id, object.getValue());
            }
        }
    }

    public void addEntityToComponent(Entity entity, Class<?> clazz, Object object) {
        addEntityToComponent(entity, getDiscriminatorForClass(clazz), object);
    }

    public void addEntityToComponent(Entity entity, Discriminator component, Object object) {
        semaphoring.modificationLockObtain();

        addEntity(entity);
        if (!objectsPerComponent.containsKey(component)) {
            objectsPerComponent.put(component, new HashMap<>());
        }
        var systemObjects = objectsPerComponent.get(component);
        systemObjects.put(entity.getId(), object);
        components.add(component);

        invalidateCacheIfSystemIsActive(entity);

        semaphoring.modificationLockRelease();
    }

    private void removeEntity(Entity entity) {
        semaphoring.modificationLockObtain();
        invalidateCacheIfSystemIsActive(entity);
        entityList.set(entity.getId(), null);

        semaphoring.modificationLockRelease();
    }

    private void addEntity(Entity entity) {
        if (entityList.size() == entity.getId()) {
            entityList.add(entity);
            return;
        }
        if (entityList.size() > entity.getId()) {
            return;
        }
        Logger.getLogger().logWarning("Warning: adding entity of id: " + entity.getId() + ", when there is a gap (entitylist has " + entityList.size() + ") elements");
        int gapSize = entity.getId() - entityList.size() - 1;
        for (int i = 0; i < gapSize; i++) {
            entityList.add(null);
        }
        entityList.add(entity);
    }

    private final Map<Class<?>, Discriminator> discriminatorForClass = Collections.synchronizedMap(new HashMap<>());

    public Discriminator[] getDiscriminatorForClass(Class<?>[] someClass) {
        Discriminator[] discriminators = new Discriminator[someClass.length]; //todo possible optimization

        for (int i = 0; i < someClass.length; i++) {
            Class<?> clazz = someClass[i];

            if (discriminatorForClass.containsKey(clazz)) {
                discriminators[i] = discriminatorForClass.get(clazz);
                continue;
            }

            semaphoring.discriminatorForClassAcquire();

            if (!discriminatorForClass.containsKey(clazz)) {
                discriminatorForClass.put(clazz, new Discriminator() {
                });
            }
            discriminators[i] = discriminatorForClass.get(clazz);

            semaphoring.discriminatorForClassRelease();
        }
        return discriminators;
    }

    public <T> Discriminator getDiscriminatorForClass(Class<T> classOfT, String suggestedName) {
        if (discriminatorForClass.containsKey(classOfT)) {
            return discriminatorForClass.get(classOfT);
        }

        semaphoring.discriminatorForClassAcquire();
        if (!discriminatorForClass.containsKey(classOfT)) {
            if (suggestedName == null) {
                discriminatorForClass.put(classOfT, new Discriminator() {
                    @Override
                    public String toString() {
                        return classOfT.getName();
                    }
                });
            } else {
                discriminatorForClass.put(classOfT, new Discriminator() {
                    @Override
                    public String toString() {
                        return suggestedName;
                    }
                });
            }
        }
        semaphoring.discriminatorForClassRelease();
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
