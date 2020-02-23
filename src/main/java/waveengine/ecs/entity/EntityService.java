package waveengine.ecs.entity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public enum EntityService {
    INSTANCE;

    private AtomicInteger entityId = new AtomicInteger(0);

    public int getNewId() {
        return entityId.getAndIncrement();
    }
}
