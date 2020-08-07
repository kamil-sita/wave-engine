package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.graphicalobject.*;
import waveengine.guiimplementation.renderingparameters.Parameters;

import static org.lwjgl.opengl.GL11.*;

public final class RendererImpl implements Renderer {
    private final WaveEngineRunning waveEngineRunning;

    public RendererImpl(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public void render(GraphicalObject graphicalObject, Parameters parameters) {

        glColor3f(1.0f, 0.5f, 0.5f);
        glBegin(GL_QUADS);
        {
            float sp = 0.5f;
            glVertex3f(-sp, -sp, 0.0f);
            glVertex3f(sp, -sp, 0.0f);
            glVertex3f(sp, sp, 0.0f);
            glVertex3f(-sp, sp, 0.0f);
        }
        glEnd();
        //renderSquare(3, 4);
        //todo

        if (!parameters.isVisible()) return;

        return;
    }

    @Override
    public void renderSquare(int x, int y) {
    }

}
