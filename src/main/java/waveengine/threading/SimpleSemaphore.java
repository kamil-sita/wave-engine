package waveengine.threading;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSemaphore {
    private final AtomicBoolean reading = new AtomicBoolean(false);
    private final AtomicInteger entries = new AtomicInteger(1);

    public void acquire() {

    }

    public void release() {

    }

    private void acquireReading() {
    }


}
