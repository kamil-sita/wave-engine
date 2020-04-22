package waveengine.library.systems;

import waveengine.Discriminator;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.core.UpdatePolicy;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.GraphicalObject;
import waveengine.guiimplementation.ImageGraphicalObject;
import waveengine.threading.AssistedReverseSemaphore;

import java.util.HashMap;


public class GraphicalResourceManager extends WaveSystem {

    private HashMap<ResourceLocation, ExpirableResource<GraphicalObject>> managedObjects = new HashMap<>();
    private AssistedReverseSemaphore<ExpirableResource<GraphicalObject>> access = new AssistedReverseSemaphore<>();


    public static void addSelf(WaveEngine waveEngine) {
        GraphicalResourceManager graphicalResourceManager = new GraphicalResourceManager();
        graphicalResourceManager.setName("Graphical Resource Manager");
        waveEngine.addSystem(UpdatePolicy.NEVER, graphicalResourceManager, GraphicalResourceManager.class);
        waveEngine.addListener(WaveEngineSystemEvents.LOW_ON_MEMORY, graphicalResourceManager::freeMemory);
    }

    @Override
    public void initialize() {
        //
    }

    @Override
    public void update(double deltaTime) {
        //
    }

    private void freeMemory(Discriminator cause, Object message) {
        access.blockAwaitDo(() -> {
            //todo - this block is in foreach, which will cause errors; automatic freeing of memory is disabled by default
            for (var expirableResourceSet : managedObjects.entrySet()) {
                var expirableResource = expirableResourceSet.getValue();
                if (expirableResource.expireIfPossible()) {
                    managedObjects.remove(expirableResourceSet.getKey());
                }
            }
        });
    }

    public ExpirableResource<GraphicalObject> getResourceOrLoad(ResourceLocation location) {
        return access.doAwaitIfNotPossibleWithReturn(() -> {
            if (managedObjects.containsKey(location)) {
                return managedObjects.get(location);
            } else {
                var image = ImageGraphicalObject.load(location);
                var managedImage = new ExpirableResource<GraphicalObject>(image);
                managedObjects.put(location, managedImage);
                return managedImage;
            }
        });
    }

    @Override
    public String getCreator() {
        return "WAVE";
    }
}
