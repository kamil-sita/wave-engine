package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;

import java.awt.*;

public final class WaveCanvas {
    private Graphics2D graphics;

    WaveCanvas(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {
        Renderer.render(graphics, graphicalObject, parameters);
    }
}
