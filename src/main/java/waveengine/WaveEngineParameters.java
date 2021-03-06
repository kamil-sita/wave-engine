package waveengine;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.GraphicsCache;
import waveengine.guiimplementation.GraphicsCacheImpl;
import waveengine.guiimplementation.Renderer;
import waveengine.guiimplementation.RendererImpl;

/**
 * This class constains parameters that should be set only once and not changed during runtime.
 */
public final class WaveEngineParameters {

    public int projectWidth() {
        return 1600;
    }

    public int projectHeight() {
        return 900;
    }

    public int numberOfThreadsForParallelJobs() {
        return 4;
    }

    public boolean useSystemWaitSpinOnWait() {
        return true;
    }

    /**
     * Whether canvas in repainted before each iteration. Recommended true.
     */
    public boolean useRepaint() {
        return true;
    }

    public boolean singleThreaded() {
        return false;
    }

    /**
     * Number of layers for renderer. If default (6), you can also use provided constants for help.
     */
    public int layerCount() {
        return 6;
    }

    /**
     * Denotes whether WaveEngine should run in strict mode, where it checks for some common errors that may come from
     * its misuse. Generally should be enabled for test/debug environments and disabled once all bugs (or most of them)
     * are eliminated.
     */
    public boolean strictMode() {
        return true;
    }

    public Renderer getRenderer(WaveEngineRunning waveEngineRunning) {
        return new RendererImpl(waveEngineRunning);
    }

    public GraphicsCache getGraphicsCache() {
        return new GraphicsCacheImpl();
    }
}
