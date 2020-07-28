package waveengine.guiimplementation.graphicalobject;

import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;

public interface GraphicalObject {
    default void dispose() {

    }

    default boolean supportsCache() {
        return true;
    }

    default void render(Graphics2D graphics2D, Parameters parameters) {

    }

    default void renderCache(Graphics2D graphics2D, float rotation, float scale, float alpha) {

    }

    int width();

    int height();
}
