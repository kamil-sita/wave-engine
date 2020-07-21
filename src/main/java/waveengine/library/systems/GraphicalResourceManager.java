package waveengine.library.systems;

import waveengine.Discriminator;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineRunning;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.core.UpdatePolicy;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.graphicalobject.ImageGraphicalObject;
import waveengine.util.Pair;

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
        if (stage.equals(actualStage)) {
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

    private void stageChange(Discriminator cause, Object message, WaveEngineRunning waveEngineRunning) {
        Discriminator stage = (Discriminator) message;

        actualStage = stage;

        ArrayList<ResourceLocation> resourcesToLoad = new ArrayList<>();

        for (Pair<Discriminator, ResourceLocation> resource : resources) {
            if (resource.getT().equals(stage)) {
                resourcesToLoad.add(resource.getU());
            }
        }

        List<ResourceLocation> toRemove = new ArrayList<>();

        for (ResourceLocation resourceLocation : loadedObjects.keySet()) {
            if (!resourcesToLoad.contains(resourceLocation)) {
                loadedObjects.get(resourceLocation).dispose();
                toRemove.add(resourceLocation);
            }
        }

        for (ResourceLocation resourceLocation : toRemove) {
            loadedObjects.remove(resourceLocation);
        }

        for (ResourceLocation resourceLocation : resourcesToLoad) {
            if (!loadedObjects.containsKey(resourceLocation)) {
                loadedObjects.put(resourceLocation, ImageGraphicalObject.load(resourceLocation));
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
