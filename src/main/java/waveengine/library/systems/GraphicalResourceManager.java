package waveengine.library.systems;

import waveengine.Discriminator;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.core.UpdatePolicy;
import waveengine.ecs.entity.EntityActiveOneStage;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.GraphicalObject;
import waveengine.guiimplementation.ImageGraphicalObject;
import waveengine.threading.AssistedReverseSemaphore;
import waveengine.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GraphicalResourceManager extends WaveSystem {


    private Map<ResourceLocation, GraphicalObject> loadedObjects = new HashMap<>();
    private List<Pair<Discriminator, ResourceLocation>> resources = new ArrayList<>();
    private Discriminator actualStage;

    public static void addSelf(WaveEngine waveEngine) {
        GraphicalResourceManager graphicalResourceManager = new GraphicalResourceManager();
        graphicalResourceManager.setName("Graphical Resource Manager");
        waveEngine.addSystem(UpdatePolicy.NEVER, graphicalResourceManager, GraphicalResourceManager.class);
        waveEngine.addListener(WaveEngineSystemEvents.STAGE_CHANGED, graphicalResourceManager::stageChange);
    }

    public void addResource(Discriminator stage, ResourceLocation resourceLocation) {
        resources.add(Pair.of(stage, resourceLocation));
        if (actualStage.equals(stage)) {
            loadResource(resourceLocation);
        }
    }

    @Override
    public void initialize() {
        //
    }

    @Override
    public void update(double deltaTime) {
        //
    }

    private void stageChange(Discriminator cause, Object message) {
        for (ResourceLocation resourceLocation : loadedObjects.keySet()) {
            loadedObjects.get(resourceLocation).dispose();
        }
        loadedObjects.clear();

        Discriminator stage = (Discriminator) message;
        actualStage = stage;

        for (Pair<Discriminator, ResourceLocation> resource : resources) {
            if (resource.getT().equals(stage)) {
                loadedObjects.put(resource.getU(), ImageGraphicalObject.load(resource.getU()));
            }
        }
    }

    private void loadResource(ResourceLocation location) {
        loadedObjects.put(location, ImageGraphicalObject.load(location));
    }

    public GraphicalObject getResource(ResourceLocation location) {
        return loadedObjects.get(location);
    }

    @Override
    public String getCreator() {
        return "WAVE";
    }
}
