package waveengine.guiimplementation;

import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;


public interface Renderer {
    void render(GraphicalObject graphicalObject, Parameters parameters);
    void renderSquare(int x, int y);
}
