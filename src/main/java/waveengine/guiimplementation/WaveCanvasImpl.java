package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class WaveCanvasImpl implements WaveCanvas {
    private final Graphics2D graphics;

    private final List<GraphicalObject>[] renderQueueGraphicalObject;
    private final List<Parameters>[] renderQueueParameters;
    private final boolean isStrict;
    private final List<Integer>[] modCount;
    private final WaveEngineRunning waveEngineRunning;
    private final Renderer renderer;

    WaveCanvasImpl(Graphics2D graphics, WaveEngineRunning waveEngineRunning, Renderer renderer) {
        this.renderer = renderer;
        this.isStrict = waveEngineRunning.getWaveEngineParameters().strictMode();
        this.waveEngineRunning = waveEngineRunning;

        int layerCount = waveEngineRunning.getWaveEngineParameters().layerCount();
        renderQueueGraphicalObject = new List[layerCount];
        renderQueueParameters = new List[layerCount];
        modCount = new List[layerCount];
        for (int i = 0; i < layerCount; i++) {
            renderQueueGraphicalObject[i] = new ArrayList<>(65536);
            renderQueueParameters[i] = new ArrayList<>(65536);
            if (isStrict) {
                modCount[i] = new ArrayList<>(65536);
            }
        }
        this.graphics = graphics;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {
        render(graphicalObject, parameters, 0);
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters, int layer) {
        renderQueueGraphicalObject[layer].add(graphicalObject);
        renderQueueParameters[layer].add(parameters);
        modCount[layer].add(parameters.getModCount());
    }

    public void renderQueue() {
        for (int i = 0; i < renderQueueGraphicalObject.length; i++) {
            for (int j = 0; j < renderQueueGraphicalObject[0].size(); j++) {
                if (isStrict) {
                    if (modCount[i].get(j) != renderQueueParameters[i].get(j).getModCount()) {
                        waveEngineRunning.shutdown(
                                "Parameter modification count differs from from expected count. " +
                                "Parameters should not be modified in iteration after passing them to renderer.",
                                true
                        );
                    }
                }
                renderer.render(
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
