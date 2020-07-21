package waveengine.guiimplementation.graphicalobject;

import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;

public interface GraphicalObject {
    default void dispose() {

    }

    default void render(Graphics2D graphics2D, Parameters parameters) {

    }
}
