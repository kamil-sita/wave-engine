package waveengine.guiimplementation;

import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;

public interface Renderer {
    void render(Graphics2D canvas, GraphicalObject graphicalObject, Parameters parameters, GraphicsCache cache);
}
