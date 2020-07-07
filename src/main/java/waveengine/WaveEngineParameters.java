package waveengine;

public final class WaveEngineParameters {
    public int numberOfThreadsForParallelJobs() {
        return 4;
    }

    public int numberOfThreadsForAfterGraphicsJobs() {
        return 4;
    }

    public boolean useSystemWaitSpinOnWait() {
        return true;
    }

    public boolean useRepaint() {
        return true;
    }

    public boolean singleThreaded() {
        return false;
    }

    /**
     * Number of layers for renderer. If default, you can also use provided constants for help.
     */
    public int layerCount() {
        return 5;
    }
}
