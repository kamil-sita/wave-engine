package waveengine.threading;

import java.util.concurrent.atomic.AtomicInteger;

public class AssistedReverseSemaphore<T> {
    private volatile AtomicInteger accessors = new AtomicInteger(0);
    private volatile boolean blockingAccess = false;

    public AssistedReverseSemaphore() {

    }

    private void getAccess() {
        while (blockingAccess) {
            Thread.onSpinWait();
        }
        accessors.incrementAndGet();
    }

    private void relieveAccess() {
        accessors.decrementAndGet();
    }

    public T doAwaitIfNotPossibleWithReturn(AssistedAccessParameterized<T> assistedAccess) {
        try {
            getAccess();
            return assistedAccess.doSth();
        } finally {
            relieveAccess();
        }
    }

    public T blockAwaitDoWithReturn(AssistedAccessParameterized<T> assistedAccess) {
        synchronized (this) {
            blockingAccess = true;
            while (accessors.get() != 0) {
                Thread.onSpinWait();
            }
            var ret = assistedAccess.doSth();
            blockingAccess = false;
            return ret;
        }
    }

    public void doAwaitIfNotPossible(AssistedAccess assistedAccess) {
        try {
            getAccess();
            assistedAccess.doSth();
        } finally {
            relieveAccess();
        }
    }

    public void blockAwaitDo(AssistedAccess assistedAccess) {
        synchronized (this) {
            blockingAccess = true;
            while (accessors.get() != 0) {
                Thread.onSpinWait();
            }
            assistedAccess.doSth();
            blockingAccess = false;
        }
    }


    @FunctionalInterface
    public interface AssistedAccessParameterized<T> {
        T doSth();
    }

    @FunctionalInterface
    public interface AssistedAccess {
        void doSth();
    }

}
