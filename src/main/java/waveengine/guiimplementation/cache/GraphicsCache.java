package waveengine.guiimplementation.cache;

import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.image.BufferedImage;

public interface GraphicsCache {
    BufferedImage get(GraphicalObject graphicalObject, Parameters parameters);
    void clear();
}
