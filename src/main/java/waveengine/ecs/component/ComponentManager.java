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

public class ComponentManager {

    private Discriminator activeStage;

    private Map<Discriminator, HashMap<Integer, Object>> objectsPerComponent = new HashMap<>();
    private Map<Discriminator, HashMap<Integer, Object>> activeObjectsPerComponent = new HashMap<>();
    private Map<List<Discriminator>, TableGroup> activeObjectsPerComponents = new HashMap<>();
    private List<Entity> entityList = new ArrayList<>();
    private Set<Discriminator> components = new HashSet<>();


    public ComponentManager(WaveEngineRunning waveEngineRunning) {
        waveEngineRunning.getNotifyingService().addListener(WaveEngineSystemEvents.STAGE_CHANGED, new NotifyingService.Notifier() {
            @Override
            public void notifyListener(Discriminator cause, Object message) {
                setActiveStage((Discriminator) message);
            }
        });
    }

    private ComponentManager setActiveStage(Discriminator activeStage) {
        modificationSemaphore.acquireUninterruptibly();
        invalidateCache();
        this.activeStage = activeStage;
        modificationSemaphore.release();
        return this;
    }

    private boolean needsToRebuildList = true;

    private Semaphore modificationSemaphore = new Semaphore(1);
    private Map<Discriminator, Semaphore> discriminatorSemaphoreMap = new ConcurrentHashMap<>();

    /**
     * Method that locks given tables, and returns combination of those tables as ManagedTableGroup object.
     * @param selectedTables tables that are required.
     * @return ManagedTableGroup object with required tables.
     */
    public ManagedTableGroup getTables(Discriminator... selectedTables) {
        var compList = Arrays.asList(selectedTables);
        acquireLockOn(compList);
        var tables = lazyGetTables(compList);
        return new ManagedTableGroup(
                tables,
                () -> releaseLockOn(compList));
    }

    private TableGroup lazyGetTables(List<Discriminator> components) {
        modificationSemaphore.acquireUninterruptibly();

        if (activeObjectsPerComponents.containsKey(components)) {
            var retVal = activeObjectsPerComponents.get(components);
            modificationSemaphore.release();
            return retVal;
        }

        if (needsToRebuildList) {
            rebuildActiveObjectsPerComponent();
            needsToRebuildList = false;
        }

        for (var component : components) {
            buildForSystem(component);
        }

        var as = new ComponentContainerImpl();
        for (var component : components) {
            as.add(component, activeObjectsPerComponent.get(component));
        }
        activeObjectsPerComponents.put(components, as);
        modificationSemaphore.release();
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
    private void acquireLockOn(List<Discriminator> components) {
        sortLockOrder(components);
        for (var component : components) {
            synchronized (component) {
                discriminatorSemaphoreMap.putIfAbsent(component, new Semaphore(1));
                discriminatorSemaphoreMap.get(component).acquireUninterruptibly();
            }

        }
    }

    private void releaseLockOn(List<Discriminator> components) {
        for (var component : components) {
            var semaphore = discriminatorSemaphoreMap.get(component);
            semaphore.release();
        }
    }

    private void sortLockOrder(List<Discriminator> systems) {
        systems.sort(Comparator.comparingLong(Discriminator::hashCode));
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
        var objectForSystem = objectsPerComponent.get(discriminator);
        for (var object : objectForSystem.entrySet()) {
            int id = object.getKey();
            if (entityList.get(id).isActive(discriminator)) {
                map.put(id, object.getValue());
            }
        }
    }

    public void addEntityToComponent(Entity entity, Discriminator component, Object object) {
        modificationSemaphore.acquireUninterruptibly();

        addEntity(entity);
        if (!objectsPerComponent.containsKey(component)) {
            objectsPerComponent.put(component, new HashMap<>());
        }
        var systemObjects = objectsPerComponent.get(component);
        systemObjects.put(entity.getId(), object);
        components.add(component);

        invalidateCacheIfSystemIsActive(entity);

        modificationSemaphore.release();
    }

    private void removeEntity(Entity entity) {
        modificationSemaphore.acquireUninterruptibly();
        invalidateCacheIfSystemIsActive(entity);
        entityList.set(entity.getId(), null);

        modificationSemaphore.release();
    }

    private void addEntity(Entity entity) {
        if (entityList.size() == entity.getId()) {
            entityList.add(entity);
            return;
        }
        if (entityList.size() > entity.getId()) {
            return;
        }
        Logger.getLogger().log("Warning: adding entity of id: " + entity.getId() + ", when there is a gap (entitylist has " + entityList.size() + ") elements");
        int gapSize = entity.getId() - entityList.size() - 1;
        for (int i = 0; i < gapSize; i++) {
            entityList.add(null);
        }
        entityList.add(entity);
    }

    /**
     * Used by handles provided by this ComponentManager to release resources (tables that are locked).
     */
    @FunctionalInterface
    public interface RemoveLockInterface {
        void remove();
    }

}
