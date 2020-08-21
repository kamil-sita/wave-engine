package waveengine.guiimplementation.graphicalobject;

import waveengine.guiimplementation.renderingparameters.Parameters;


public interface GraphicalObject {
    float getWidth();
    float getHeight();

    void render(Parameters parameters, float[] bounds);

    default void dispose() {

    }

}
