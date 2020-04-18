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

    public boolean useMemoryFreeingService() {return false;}

    public boolean useRepaint() {
        return true;
    }

    public boolean singleThreaded() {
        return true;
    }

    public int acquireResourceRetries() {
        return 3;
    }

    public long acquireResourceSingleRequestTime() {
        return 400;
    }
}
