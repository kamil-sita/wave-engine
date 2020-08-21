package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.util.ArrayList;
import java.util.List;

public final class WaveCanvasImpl implements WaveCanvas {

    private final List<GraphicalObject>[] renderQueueGraphicalObject;
    private final List<Parameters>[] renderQueueParameters;
    private final boolean isStrict;
    private final List<Integer>[] modCount;
    private final WaveEngineRunning waveEngineRunning;
    private final Renderer renderer;
    private final int layerCount; //layer count without debug layer
    private int renderedObjects = 0;

    WaveCanvasImpl(WaveEngineRunning waveEngineRunning, Renderer renderer) {
        this.renderer = renderer;
        this.isStrict = waveEngineRunning.getWaveEngineParameters().strictMode();
        this.waveEngineRunning = waveEngineRunning;

        layerCount = waveEngineRunning.getWaveEngineParameters().layerCount();
        renderQueueGraphicalObject = new List[layerCount + 1];
        renderQueueParameters = new List[layerCount + 1];
        modCount = new List[layerCount + 1];
        for (int i = 0; i < layerCount + 1; i++) {
            renderQueueGraphicalObject[i] = new ArrayList<>(65536);
            renderQueueParameters[i] = new ArrayList<>(65536);
            if (isStrict) {
                modCount[i] = new ArrayList<>(65536);
            }
        }
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {
        render(graphicalObject, parameters, 0);
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters, int layer) {
        renderQueueGraphicalObject[layer].add(graphicalObject);
        renderQueueParameters[layer].add(parameters);
        if (isStrict) {
            modCount[layer].add(parameters.getModCount());
        }
        renderedObjects++;
    }

    @Override
    public int getLayerCount() {
        return layerCount;
    }

    public void renderQueue(int width, int height) {
        if (renderedObjects == 0) {
            return;
        }

        for (int i = 0; i < renderQueueGraphicalObject.length; i++) {
            for (int j = 0; j < renderQueueGraphicalObject[i].size(); j++) {
                if (isStrict) {
                    if (modCount[i].get(j) != renderQueueParameters[i].get(j).getModCount()) {
                        waveEngineRunning.shutdown(
                                "Parameter modification count differs from from expected count. " +
                                "Parameters should not be modified in iteration after passing them to renderer. \r\n" +
                                "Expected: " + renderQueueParameters[i].get(j).getModCount() + ", got: " + modCount[i].get(j),
                                true
                        );
                    }
                }
                renderer.render(
                        renderQueueGraphicalObject[i].get(j),
                        renderQueueParameters[i].get(j),
                        width,
                        height
                );
            }
            renderQueueGraphicalObject[i].clear();
            renderQueueParameters[i].clear();
            if (isStrict) {
                modCount[i].clear();
            }
        }

        renderedObjects = 0;
    }

}
