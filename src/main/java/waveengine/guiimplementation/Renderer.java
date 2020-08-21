package waveengine.guiimplementation;

import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;


public interface Renderer {
    void render(GraphicalObject graphicalObject, Parameters parameters, int width, int height);
}
