package waveengine.threading;

import java.util.concurrent.atomic.AtomicInteger;

public class ReverseSemaphore {
    private volatile AtomicInteger accessors = new AtomicInteger(0);
    private volatile boolean blockingAccess = false;

    public ReverseSemaphore() {

    }

    public void getAccess() {
        while (blockingAccess) {
            Thread.onSpinWait();
        }
        accessors.incrementAndGet();
    }

    public void relieveAccess() {
        accessors.decrementAndGet();
    }


    public void blockAccess() {
        blockingAccess = true;
        while (accessors.get() != 0) {
            Thread.onSpinWait();
        }
    }

    public void stopBlockingAccess() {
        blockingAccess = false;
    }


}
