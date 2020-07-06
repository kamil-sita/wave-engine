package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class WaveCanvas {
    private final Graphics2D graphics;

    private final List<GraphicalObject>[] renderQueueGraphicalObject;
    private final List<Parameters>[] renderQueueParameters;


    WaveCanvas(Graphics2D graphics, WaveEngineRunning waveEngineRunning) {
        int layerCount = waveEngineRunning.getWaveEngineParameters().layerCount();
        renderQueueGraphicalObject = new List[layerCount];
        renderQueueParameters = new List[layerCount];
        for (int i = 0; i < layerCount; i++) {
            //renderQueueGraphicalObject[i] = new DirtyList<>(65536);
            //renderQueueParameters[i] = new DirtyList<>(65536);
            renderQueueGraphicalObject[i] = new ArrayList<>(65536);
            renderQueueParameters[i] = new ArrayList<>(65536);
        }
        this.graphics = graphics;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {
        render(graphicalObject, parameters, 0);
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters, int layer) {
        renderQueueGraphicalObject[layer].add(graphicalObject);
        renderQueueParameters[layer].add(parameters);
    }

    public void renderQueue() {
        for (int i = 0; i < renderQueueGraphicalObject.length; i++) {
            for (int j = 0; j < renderQueueGraphicalObject[0].size(); j++) {
                Renderer.render(
                        graphics,
                        renderQueueGraphicalObject[i].get(j),
                        renderQueueParameters[i].get(j)
                );
            }
            renderQueueGraphicalObject[i].clear();
            renderQueueParameters[i].clear();
        }
    }
}
