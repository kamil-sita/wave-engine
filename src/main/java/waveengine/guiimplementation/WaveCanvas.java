package waveengine.guiimplementation;


import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

public interface WaveCanvas {

    int BACKGROUND = 0;
    int BACKGROUND_PARTICLES = 1;
    int MIDDLE_GROUND = 2;
    int MIDDLE_GROUND_PARTICLES = 3;
    int FOREGROUND = 4;
    int FOREGROUND_PARTICLES = 5;

    void render(GraphicalObject graphicalObject, Parameters parameters);

    void render(GraphicalObject graphicalObject, Parameters parameters, int layer);

    int getLayerCount();

    default void renderDebug(GraphicalObject graphicalObject, Parameters parameters) {
        render(graphicalObject, parameters, getLayerCount());
    }

}