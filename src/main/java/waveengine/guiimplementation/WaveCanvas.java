package waveengine.guiimplementation;


import waveengine.guiimplementation.renderingparameters.Parameters;

public interface WaveCanvas {
    void render(GraphicalObject graphicalObject, Parameters parameters);
    void render(GraphicalObject graphicalObject, Parameters parameters, int layer);
}