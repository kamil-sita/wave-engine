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
        return false;
    }

    public int acquireResourceRetries() {
        return 5;
    }

    public long acquireResourceSingleRequestTime() {
        return 400;
    }
}
