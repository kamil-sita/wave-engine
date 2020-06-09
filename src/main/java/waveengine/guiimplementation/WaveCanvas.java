package waveengine.guiimplementation;

import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public final class WaveCanvas {
    private final Graphics2D graphics;

    private final List<Pair<GraphicalObject, Parameters>> renderQueue = new ArrayList<>();
    private final Semaphore semaphore = new Semaphore(1);


    WaveCanvas(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {
        addToQueue(graphicalObject, parameters);
    }

    public void renderNow(GraphicalObject graphicalObject, Parameters parameters) {
        Renderer.render(graphics, graphicalObject, parameters);
    }
    public void addToQueue(GraphicalObject graphicalObject, Parameters parameters) {
        semaphore.acquireUninterruptibly();
        renderQueue.add(new Pair<>(graphicalObject, parameters));
        semaphore.release();
    }

    public void renderQueue() {
        semaphore.acquireUninterruptibly();
        for (Pair<GraphicalObject, Parameters> pair : renderQueue) {
            renderNow(pair.getT(), pair.getU());
        }
        renderQueue.clear();
        semaphore.release();
    }
}
