package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.graphicalobject.*;
import waveengine.guiimplementation.renderingparameters.Parameters;

public final class RendererImpl implements Renderer {
    private final WaveEngineRunning waveEngineRunning;
    private final float[] boundsArray = new float[4];

    public RendererImpl(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters, int windowWidth, int windowHeight) {
        if (waveEngineRunning.getWaveEngineParameters().strictMode()) {
            if (graphicalObject == null) {
                throw new RuntimeException("Graphical object cannot be null");
            }
            if (parameters == null) {
                throw new RuntimeException("Parameters cannot be null");
            }
        }


        if (!parameters.isVisible()) return;

        int projectWidth = waveEngineRunning.getWaveEngineParameters().projectWidth();
        int projectHeight = waveEngineRunning.getWaveEngineParameters().projectHeight();

        parameters.getPositioning().getBounds(boundsArray, parameters.getX(), parameters.getY(), graphicalObject.getWidth() * parameters.getScale(), graphicalObject.getHeight() * parameters.getScale(), projectWidth, projectHeight);

        graphicalObject.render(parameters, boundsArray);
    }

}
